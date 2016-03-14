package com.apm4all.tracy.backend;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.apache.camel.Headers;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filters.Filters;
import org.elasticsearch.search.aggregations.bucket.filters.Filters.Bucket;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentile;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;

import com.apm4all.tracy.apimodel.LatencyHistogram;
import com.apm4all.tracy.apimodel.SingleApdexTimechart;
import com.apm4all.tracy.apimodel.TaskConfig;
import com.apm4all.tracy.apimodel.TaskMeasurement;
import com.apm4all.tracy.apimodel.VitalsTimechart;
import com.apm4all.tracy.util.LatencyHistogramRows;
import com.apm4all.tracy.util.LatencyHistogramRows.LatencyHistogramRow;
import com.apm4all.tracy.util.TimeFrame;

public class EsQueryProcessor {
	public static final String APPLICATION = "application";
	public static final String TASK = "task";
	public static final String TASK_CONFIG = "taskConfig";
	public static final String TIME_FRAME = "timeFrame";
	public static final String EARLIEST = "earliest";
	public static final String LATEST = "latest";
	public static final String SNAP = "snap";
	public static final String TASK_MEASUREMENT = "taskMeasurement";
	
	public void initMeasurement(@Headers Map<String, Object> headers,
			@Header(APPLICATION) String application,
			@Header(TASK) String task,
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
		singleApdexTimechart.setApplication(application);
		singleApdexTimechart.setTask(task);
		singleApdexTimechart.setRttF(taskConfig.getMeasurement().getRttFrustrated());
		singleApdexTimechart.setRttT(taskConfig.getMeasurement().getRttTolerating());
		singleApdexTimechart.setRttUnit(taskConfig.getMeasurement().getRttUnit());
	}
	
	
	public XContentBuilder buildOverviewSearchRequest(
			@Header(TASK_CONFIG) TaskConfig taskConfig, 
			@Header(TIME_FRAME) TimeFrame timeFrame) throws IOException	{
		// "bool" Restrict results by time range and match criteria
		
		long earliest = timeFrame.getEarliest();
		long latest = timeFrame.getLatest();
		int rttTolerating = taskConfig.getMeasurement().getRttTolerating();
		int rttFrustrated = taskConfig.getMeasurement().getRttFrustrated();
		String taskDefiningFilter = taskConfig.getDefiningFilter();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				// TODO: For improved performance, filtering should be done at index level as well to avoid accessing unnecessary indexes
				.must(QueryBuilders.rangeQuery("@timestamp").gt(earliest).lt(latest))
				.must(QueryBuilders.queryStringQuery(taskDefiningFilter));
		// "aggs" date histogram aggregation
		@SuppressWarnings("rawtypes")
		AggregationBuilder aggregationBuilder = AggregationBuilders
			.dateHistogram("timeBuckets")
		    .field("@timestamp")
		    .interval(DateHistogram.Interval.MINUTE)
		    // min_doc_count does not seem to work with DateHistogram.
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
				// When handling response need to fill-in for empty (not returned) buckets
				// min_doc_count does not seem to work with DateHistogram.
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
		// Populate SingleApdexTimechart: timeSequence, apdexScores
		SingleApdexTimechart singleApdexTimechart = taskMeasurement.getSingleApdexTimechart();
		singleApdexTimechart.setTimeSequence(apdexTimeSequence);
		singleApdexTimechart.setApdexScores(apdexScores);
		
		// Populate VitalsTimeChart: timeSequence, count, errors (skip p95 and max)
		VitalsTimechart vitalsTimeChart = taskMeasurement.getVitalsTimechart(); 
		vitalsTimeChart.setTimeSequence(vitalsTimeSequence);
		vitalsTimeChart.setCount(vitalsCount);
		vitalsTimeChart.setErrors(vitalsErrors);
		
		return taskMeasurement;
	}

	public XContentBuilder buildSuccessStatsSearchRequest(
			@Header(TASK_CONFIG) TaskConfig taskConfig, 
			@Header(TIME_FRAME) TimeFrame timeFrame) throws IOException	{
		// "bool" Restrict results by time range and match criteria
		
		long earliest = timeFrame.getEarliest();
		long latest = timeFrame.getLatest();
		int rttTolerating = taskConfig.getMeasurement().getRttTolerating();
		int rttFrustrated = taskConfig.getMeasurement().getRttFrustrated();
		String taskDefiningFilter = taskConfig.getDefiningFilter();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				// TODO: For improved performance, filtering should be done at index level as well to avoid accessing unnecessary indexes
				.must(QueryBuilders.rangeQuery("@timestamp").gt(earliest).lt(latest))
				.must(QueryBuilders.queryStringQuery(taskDefiningFilter));
		// "aggs" date histogram aggregation
		@SuppressWarnings("rawtypes")
		AggregationBuilder aggregationBuilder = AggregationBuilders
			.dateHistogram("timeBuckets")
		    .field("@timestamp")
		    .interval(DateHistogram.Interval.MINUTE)
		    // min_doc_count does not seem to work with DateHistogram.
		    .minDocCount(0) 
		    .subAggregation(
		    	    AggregationBuilders
		    	        .filters("counters")
		    	            .filter("success", FilterBuilders.rangeFilter("status").lt(500))
		    	            	.subAggregation(AggregationBuilders
		    	            		.percentiles("percentiles")
		    	            		.field("msecElapsed")
		    	            		.percentiles(50.0, 95.0, 99.0))
		    	            	.subAggregation(AggregationBuilders
		    	            		.stats("stats")
		    	            		.field("msecElapsed"))
		    	            	.subAggregation(AggregationBuilders
		    	            		.filters("latencyHistogram")
		    	            		.filter("0-100", FilterBuilders.rangeFilter("msecElapsed").gt(0).lte(100))
		    	            		.filter("100-200", FilterBuilders.rangeFilter("msecElapsed").gt(100).lte(200))
		    	            		.filter("200-300", FilterBuilders.rangeFilter("msecElapsed").gt(200).lte(300))
		    	            		.filter("300-400", FilterBuilders.rangeFilter("msecElapsed").gt(300).lte(400))
		    	            		.filter("400-500", FilterBuilders.rangeFilter("msecElapsed").gt(400).lte(500))
		    	            		.filter("500-600", FilterBuilders.rangeFilter("msecElapsed").gt(600).lte(600))
		    	            		.filter("600-700", FilterBuilders.rangeFilter("msecElapsed").gt(600).lte(700))
		    	            		.filter("700-800", FilterBuilders.rangeFilter("msecElapsed").gt(700).lte(800))
		    	            		.filter(">800", FilterBuilders.rangeFilter("msecElapsed").gt(800))
		    		));
		
		// TODO: Populate latencyHistogramFilters
//        LatencyHistogramRows latencyHistogramRows = new LatencyHistogramRows(
//                        taskConfig.getMeasurement().getRttTolerating(), 
//                        taskConfig.getMeasurement().getRttFrustrated());
//        for (LatencyHistogramRow row : latencyHistogramRows.asList())	{ // Sort by latency descending
//                // TODO: create filter using 
//        		if (row.getUpperLimit != null)	{
//        			.filter(row.getLabel()), FilterBuilders.rangeFilter("msecElapsed").gt(row.getLowerLimit()).lte(row.getUpperLimit()));
//        		}
//        		else	{
//        			.filter(row.getLabel()), FilterBuilders.rangeFilter("msecElapsed").gt(row.getLowerLimit()));
//        		}
//        		row.
//                System.out.println("latencyHistogram [" + row.getLabel() // 0-100
//                                + "], value [" +  latencyHistogram.getBucketByKey(row.getLabel()).getDocCount() + "]");
//        }

		
		
        XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject().field("query");
        queryBuilder.toXContent(contentBuilder, null);
        contentBuilder.startObject("aggs");
        aggregationBuilder.toXContent(contentBuilder, null);
        contentBuilder.endObject();
        contentBuilder.field("size", 0);
        contentBuilder.endObject();
//        System.out.println("*** Query ***");
//        System.out.println(contentBuilder.string());
		return contentBuilder;
	}


	public TaskMeasurement handleSucessStatsSearchResponse(
			@Header(TASK_CONFIG) TaskConfig taskConfig, 
			@Header(TIME_FRAME) TimeFrame timeFrame,
			@Header(TASK_MEASUREMENT) TaskMeasurement taskMeasurement,
			@Body SearchResponse searchResponse) {
		// Vitals data
		ArrayList<Double> p95 = new ArrayList<Double>();
		ArrayList<Double> max = new ArrayList<Double>();
		// LatencyHistogram data
		ArrayList<String> bins = new ArrayList<String>();
		ArrayList<String> rttZone = new ArrayList<String>();
		ArrayList<Integer> count = new ArrayList<Integer>();
		LatencyHistogramRows latencyHistogramRows = new LatencyHistogramRows(
				taskConfig.getMeasurement().getRttTolerating(), 
				taskConfig.getMeasurement().getRttFrustrated());
		for (LatencyHistogramRow row : latencyHistogramRows.asList())	{
			bins.add(row.getLabel());
			rttZone.add(row.getRttZone());
			count.add(0);
		}
		
		DateHistogram agg = searchResponse.getAggregations().get("timeBuckets");
		for (long time=timeFrame.getEarliest() ; time < timeFrame.getLatest() ; time = time+timeFrame.getSnap())	{
			DateHistogram.Bucket histogramBucket = agg.getBucketByKey(time);
			if (histogramBucket != null)	{
				// If ES response contains this time bucket, populate from ES
				Filters counters = histogramBucket.getAggregations().get("counters");
				Bucket satisfiedBucket = counters.getBucketByKey("success");
				Stats stats = satisfiedBucket.getAggregations().get("stats");
                // Populate Max
				System.out.println("Max [" + stats.getMax() + "]");
				max.add(stats.getMax());
				
				Percentiles percentiles = satisfiedBucket.getAggregations().get("percentiles");
				
				for (Percentile entry : percentiles) {
				    double percent = entry.getPercent();    // Percent
				    if (percent == 95.0)	{
                        // Populate p95
				    	double value = entry.getValue();        // Value
				    	System.out.println("percent [" + percent + "], value [" +  value + "]");
				    	p95.add(value);
				    }
				}
			
				Filters latencyHistogram = satisfiedBucket.getAggregations().get("latencyHistogram");
                //TODO: Use latencyHistogram

				int i = 0;
				for (LatencyHistogramRow row : latencyHistogramRows.asList())	{ // Sort by latency descending
					// latency histogram count
					int binCount = count.get(i);
					binCount = (int) (Integer.valueOf(binCount) + latencyHistogram.getBucketByKey(row.getLabel()).getDocCount());
					count.set(i, binCount);
					i++;
					System.out.println("latencyHistogram [" + row.getLabel() // 0-100
							+ "], value [" +  latencyHistogram.getBucketByKey(row.getLabel()).getDocCount() + "]");
				}
				
				System.out.println("latencyHistogram [0-100], value [" +  latencyHistogram.getBucketByKey("0-100").getDocCount() + "]");
				System.out.println("latencyHistogram [100-200], value [" +  latencyHistogram.getBucketByKey("100-200").getDocCount() + "]");
				System.out.println("latencyHistogram [200-300], value [" +  latencyHistogram.getBucketByKey("200-300").getDocCount() + "]");
				System.out.println("latencyHistogram [300-400], value [" +  latencyHistogram.getBucketByKey("300-400").getDocCount() + "]");
				System.out.println("latencyHistogram [400-500], value [" +  latencyHistogram.getBucketByKey("400-500").getDocCount() + "]");
				System.out.println("latencyHistogram [500-600], value [" +  latencyHistogram.getBucketByKey("500-600").getDocCount() + "]");
				System.out.println("latencyHistogram [600-700], value [" +  latencyHistogram.getBucketByKey("600-700").getDocCount() + "]");
				System.out.println("latencyHistogram [700-800], value [" +  latencyHistogram.getBucketByKey("700-800").getDocCount() + "]");
				System.out.println("latencyHistogram [>800], value [" +  latencyHistogram.getBucketByKey(">800").getDocCount() + "]");
			}
			else	{
				p95.add(null);
				max.add(null);
			}
		}
		VitalsTimechart vitalsTimechart = taskMeasurement.getVitalsTimechart();
		vitalsTimechart.setMax(max);
		vitalsTimechart.setP95(p95);
		
		// Add LatencyHistogram data
		LatencyHistogram latencyHistogram = taskMeasurement.getLatencyHistogram();
		latencyHistogram.setBins(bins);
		latencyHistogram.setCount(count);
		latencyHistogram.setRttZone(rttZone);
		
		
		
		return taskMeasurement;
	}
}