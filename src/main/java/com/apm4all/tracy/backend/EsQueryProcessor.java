package com.apm4all.tracy.backend;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Header;
import org.apache.camel.Headers;
import org.apache.camel.Body;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filters.Filters;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;

import com.apm4all.tracy.apimodel.SingleApdexTimechart;
import com.apm4all.tracy.apimodel.TaskConfig;
import com.apm4all.tracy.apimodel.TaskMeasurement;
import com.apm4all.tracy.apimodel.VitalsTimechart;
import com.apm4all.tracy.util.TimeFrame;

public class EsQueryProcessor {
	public static final String TASK_CONFIG = "taskConfig";
	public static final String TIME_FRAME = "timeFrame";
	public static final String EARLIEST = "earliest";
	public static final String LATEST = "latest";
	public static final String SNAP = "snap";
	public static final String TASK_MEASUREMENT = "taskMeasurement";
	
	public void initMeasurement(@Headers Map<String, Object> headers,
			@Header(TASK_CONFIG) TaskConfig taskConfig,
			@Header(EARLIEST) String earliest,
			@Header(LATEST) String latest,
			@Header(SNAP) String snap) {
		// FIXME: A valid config must be supplied at this point 
		if (taskConfig == null)	{
			taskConfig = new TaskConfig();
			headers.put(TASK_CONFIG, taskConfig);
		}
		// Get earliest, latest and snap if provided
		headers.put(TIME_FRAME, new TimeFrame(earliest, latest, snap, taskConfig));
		
		// Setup RetrievedTaskMeasurement
		TaskMeasurement taskMeasurement = new RetrievedTaskMeasurement(taskConfig.getApplication(), taskConfig.getTask());
		headers.put(TASK_MEASUREMENT, taskMeasurement);
	
		SingleApdexTimechart singleApdexTimechart = taskMeasurement.getSingleApdexTimechart();
		singleApdexTimechart.setApplication(taskConfig.getApplication());
		singleApdexTimechart.setRttF(taskConfig.getMeasurement().getRttFrustrated());
		singleApdexTimechart.setRttT(taskConfig.getMeasurement().getRttTolerating());
		singleApdexTimechart.setRttUnit(taskConfig.getMeasurement().getRttUnit());
		singleApdexTimechart.setTask(taskConfig.getTask());
	}
	
	
	public XContentBuilder buildOverviewSearchRequest(
			@Header(TASK_CONFIG) TaskConfig taskConfig, 
			@Header(TIME_FRAME) TimeFrame timeFrame) throws IOException	{
		// "bool" Restrict results by time range and match criteria
		
		System.out.println(timeFrame);
		long earliest = timeFrame.getEarliest();
		long latest = timeFrame.getLatest();
		int rttTolerating = taskConfig.getMeasurement().getRttTolerating();
		int rttFrustrated = taskConfig.getMeasurement().getRttFrustrated();
		String taskDefiningFilter = taskConfig.getDefiningFilter();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				// TODO: For improved performance, filtering should be done at index level as well to avoid accessing uneccessary indexes
				.must(QueryBuilders.rangeQuery("@timestamp").gt(earliest).lt(latest))
				.must(QueryBuilders.queryStringQuery(taskDefiningFilter));
		// "aggs" date histogram aggregation
		@SuppressWarnings("rawtypes")
		AggregationBuilder aggregationBuilder = AggregationBuilders
			.dateHistogram("timeBuckets")
		    .field("@timestamp")
		    .interval(DateHistogram.Interval.MINUTE)
		    // min_doc_count does not seem to work with DateHistogram.
		    // May need to use Range or else fill-in for empty (not returned) buckets
		    .minDocCount(0) 
		    .subAggregation(
		    	    AggregationBuilders
		    	        .filters("counters")
		    	            .filter("invocations", FilterBuilders.queryFilter(QueryBuilders.simpleQueryStringQuery(taskDefiningFilter)))
		    	            .filter("success", FilterBuilders.rangeFilter("status").lt(500))
		    	            .filter("errors", FilterBuilders.rangeFilter("status").gte(500))
		    	            .filter("satisfied", FilterBuilders.andFilter(
		    	            		FilterBuilders.rangeFilter("status").lt(500),
		    	            		FilterBuilders.rangeFilter("msecElapsed").lt(rttTolerating)))
		    	            .filter("tolerating", FilterBuilders.andFilter(
		    	            		FilterBuilders.rangeFilter("status").lt(500),
		    	            		FilterBuilders.rangeFilter("msecElapsed").gt(rttTolerating).lt(rttFrustrated)))
		    	            .filter("frustrated", FilterBuilders.andFilter(
		    	            		FilterBuilders.rangeFilter("status").lt(500),
		    	            		FilterBuilders.rangeFilter("msecElapsed").gt(rttFrustrated)))
		    		);
        XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject().field("query");
        queryBuilder.toXContent(contentBuilder, null);
        contentBuilder.startObject("aggs");
        aggregationBuilder.toXContent(contentBuilder, null);
        contentBuilder.endObject();
        contentBuilder.field("size", 0);
        contentBuilder.endObject();
		return contentBuilder;
	}

	private Double calculateApdexScore(long invocations, long satisfied, long tolerating) {
		Double apdexScore;
		if(invocations == 0) {
			  throw new ArithmeticException("Division by zero!");
		}
		apdexScore = (double) (((double)satisfied + ((double)tolerating/2)) / (double)invocations);
					System.out.println("apdex: " + apdexScore 
							+ ", invocations " + invocations
							+ ", satisfied " + satisfied
							+ ", tolerating " + tolerating);
		BigDecimal bd = new BigDecimal(apdexScore);
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public TaskMeasurement handleOverviewSearchResponse(
			@Header(TASK_CONFIG) TaskConfig taskConfig, 
			@Header(TIME_FRAME) TimeFrame timeFrame,
			@Header(TASK_MEASUREMENT) TaskMeasurement taskMeasurement,
			@Body SearchResponse searchResponse) {
		
		DateHistogram agg = searchResponse.getAggregations().get("timeBuckets");

		// TODO: Iterate through each time bucket
		List<Long> apdexTimeSequence = new ArrayList<Long>();
		List<Long> vitalsTimeSequence = new ArrayList<Long>();
		List<Integer> vitalsCount = new ArrayList<Integer>();
		List<Integer> vitalsErrors = new ArrayList<Integer>();
		
		
		List<Double> apdexScores = new ArrayList<Double>();
		for (long time=timeFrame.getEarliest() ; time < timeFrame.getLatest() ; time = time+timeFrame.getSnap())	{
			DateHistogram.Bucket histogramBucket = agg.getBucketByKey(time);
			long invocations = 0;
			long errors = 0;
			if (histogramBucket != null)	{
				// If ES response contains this time bucket, populate from ES
				Filters f = histogramBucket.getAggregations().get("counters");
				long satisfied = f.getBucketByKey("satisfied").getDocCount();
				long tolerating = f.getBucketByKey("tolerating").getDocCount();
				invocations = f.getBucketByKey("invocations").getDocCount();
				errors = f.getBucketByKey("errors").getDocCount();
				try	{
					Double apdexScore = calculateApdexScore(invocations, satisfied, tolerating);
					apdexTimeSequence.add(time);
					apdexScores.add(apdexScore);
					System.out.println("+++ TIME: " + time 
							+ ", apdex: " + apdexScore 
							+ ", invocations " + invocations
							+ ", satisfied " + satisfied
							+ ", tolerating " + tolerating);
				}
				catch (ArithmeticException e) {
				}
			}
			else	{
				// If ES response does not contain this time bucket, populate defaults
				System.out.println("--- TIME: " + time);
			}
			vitalsTimeSequence.add(time);
			vitalsErrors.add((int) errors);
			vitalsCount.add((int) invocations);
		}
		
		for (DateHistogram.Bucket entry : agg.getBuckets()) {
			String key = entry.getKey();                // Key
			Number nkey = entry.getKeyAsNumber();
			Filters f = entry.getAggregations().get("counters");
			for (Filters.Bucket bucket : f.getBuckets())	{
				System.out.println( "key [" + key + "], " 
						+ "epoch [" + nkey.longValue() + "], "
						+ bucket.getKey() + " [" + bucket.getDocCount() + "]");
			}
		}
		// Populate TaskMeasurement with Overview SearchResponse data
		// TODO: Populate SingleApdexTimechart: timeSequence, application, task, rttUnit, rttT, rttF, apdexScores 
		SingleApdexTimechart singleApdexTimechart = taskMeasurement.getSingleApdexTimechart();
		singleApdexTimechart.setTimeSequence(apdexTimeSequence);
		singleApdexTimechart.setApdexScores(apdexScores);
		
		// TODO: Populate VitalsTimeChart: timeSequence, count, errors (skip p95 and max)
		VitalsTimechart vitalsTimeChart = taskMeasurement.getVitalsTimechart(); 
		vitalsTimeChart.setTimeSequence(vitalsTimeSequence);
		vitalsTimeChart.setCount(vitalsCount);
		vitalsTimeChart.setErrors(vitalsErrors);
		
		return taskMeasurement;
	}


}
