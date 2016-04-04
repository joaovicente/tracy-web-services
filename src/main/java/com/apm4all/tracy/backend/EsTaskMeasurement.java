package com.apm4all.tracy.backend;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Headers;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.elasticsearch.ElasticsearchConstants;
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
import org.elasticsearch.search.aggregations.bucket.filters.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentile;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;

import com.apm4all.tracy.apimodel.LatencyHistogram;
import com.apm4all.tracy.apimodel.SingleApdexTimechart;
import com.apm4all.tracy.apimodel.TaskConfig;
import com.apm4all.tracy.apimodel.TaskMeasurement;
import com.apm4all.tracy.apimodel.VitalsTimechart;
import com.apm4all.tracy.util.LatencyHistogramRows;
import com.apm4all.tracy.util.LatencyHistogramRows.LatencyHistogramRow;
import com.apm4all.tracy.util.TimeFrame;

// TODO: Extract EsTaskMeasurement and EsTaskConfig
public class EsTaskMeasurement{
	private ProducerTemplate template;
	private EsTaskConfig esTaskConfig;

	public void setTemplate(ProducerTemplate template) {
		this.template = template;
	}

	public void setEsTaskConfig(EsTaskConfig esTaskConfig)	{
		this.esTaskConfig = esTaskConfig;
	}

	public void getTaskMeasurement(
			Exchange exchange,
			@Header(com.apm4all.tracy.apimodel.Headers.APPLICATION) String application,
			@Header(com.apm4all.tracy.apimodel.Headers.TASK) String task,
			@Header(com.apm4all.tracy.apimodel.Headers.EARLIEST) String earliest,
			@Header(com.apm4all.tracy.apimodel.Headers.LATEST) String latest,
			@Header(com.apm4all.tracy.apimodel.Headers.SNAP) String snap,
			@Headers Map<String, Object> headers) throws IOException {
	
		final String MEASUREMENT_STAGE_COMPLETED = "measurementStageCompleted";
		
		headers.put(MEASUREMENT_STAGE_COMPLETED, "started");
		try {
			// Get taskConfig for application,task 
			TaskConfig taskConfig = esTaskConfig.getTaskConfigFromEs(application, task);
			headers.put(MEASUREMENT_STAGE_COMPLETED, "gotTaskConfig");
			// Get earliest, latest and snap if provided
			TimeFrame timeFrame = new TimeFrame(earliest, latest, snap, taskConfig);
			headers.put(com.apm4all.tracy.apimodel.Headers.EARLIEST, timeFrame.getEarliest());
			headers.put(com.apm4all.tracy.apimodel.Headers.LATEST, timeFrame.getLatest());
			headers.put(com.apm4all.tracy.apimodel.Headers.SNAP, timeFrame.getSnap());
		
			TaskMeasurement taskMeasurement;
			taskMeasurement = new RetrievedTaskMeasurement(application, task);
			populateTaskMeasurementFromTaskConfig(application, task, taskConfig, taskMeasurement);

			// TODO: Refine index to tracy-<component>, but component will need to be captured in taskConfig somehow
			headers.put(ElasticsearchConstants.PARAM_INDEX_NAME, "tracy*"); 
			headers.put(ElasticsearchConstants.PARAM_INDEX_TYPE, "tracy");
			
			// Overview query: populates APDEX scores and vitals counts
			XContentBuilder contentBuilder = buildOverviewSearchRequest(taskConfig, timeFrame);
			headers.put("esOverviewSearchRequest", contentBuilder.string());
			// make Overview Search Request
			SearchResponse searchResponse = template.requestBodyAndHeaders("elasticsearch://local?operation=SEARCH", contentBuilder, headers, SearchResponse.class);
//			headers.put("esOverviewSearchResponse", searchResponse.toString());
			handleOverviewSearchResponse(timeFrame, taskMeasurement, searchResponse);
			headers.put(MEASUREMENT_STAGE_COMPLETED, "gotTaskConfig");
			headers.put(MEASUREMENT_STAGE_COMPLETED, "overviewSearchCompleted");
			
			// Success Stats query: populates vitals stats max,percentiles and latencyHistogram 
			contentBuilder = buildSuccessStatsSearchRequest(taskConfig, timeFrame);
			headers.put("esSuccessStatsSearchRequest", contentBuilder.string());
			// make Success Stats Search Request
			searchResponse = template.requestBodyAndHeaders("elasticsearch://local?operation=SEARCH", contentBuilder, headers, SearchResponse.class);
//			headers.put("esSuccessStatsSearchResponse", searchResponse.toString());
			handleSucessStatsSearchResponse(taskConfig, timeFrame, taskMeasurement, searchResponse);
			headers.put(MEASUREMENT_STAGE_COMPLETED, "successStatsSearchCompleted");
			
			exchange.getIn().setBody(taskMeasurement);
			headers.put(MEASUREMENT_STAGE_COMPLETED, "all");
		} catch (CamelExecutionException e) {
			// TODO: Improve error/exception handling and set http status
			exchange.getIn().setBody("Failed to retrieved taskMeasurement");
		}
	}

    private void populateTaskMeasurementFromTaskConfig(
            String application,
            String task,
            TaskConfig taskConfig,
            TaskMeasurement taskMeasurement)	{
        SingleApdexTimechart singleApdexTimechart = taskMeasurement.getSingleApdexTimechart();
        singleApdexTimechart.setApplication(application);
        singleApdexTimechart.setTask(task);
        singleApdexTimechart.setRttF(taskConfig.getMeasurement().getRttFrustrated());
        singleApdexTimechart.setRttT(taskConfig.getMeasurement().getRttTolerating());
        singleApdexTimechart.setRttUnit(taskConfig.getMeasurement().getRttUnit());
    }
	private Double calculateApdexScore(long invocations, long satisfied, long tolerating) {
		Double apdexScore;
		if(invocations == 0) {
			  throw new ArithmeticException("Division by zero!");
		}
		apdexScore = ((double)satisfied + ((double)tolerating/2)) / (double)invocations;
//					System.out.println("apdex: " + apdexScore 
//							+ ", invocations " + invocations
//							+ ", satisfied " + satisfied
//							+ ", tolerating " + tolerating);
		return round(apdexScore, 2);
	}
	
	private Double round(double number, int digits)	{
		BigDecimal bd = new BigDecimal(number);
		bd = bd.setScale(digits, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public XContentBuilder buildOverviewSearchRequest(
			TaskConfig taskConfig, 
			TimeFrame timeFrame) throws IOException	{
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
		    .interval(DateHistogram.Interval.seconds(taskConfig.getMeasurement().getSnap()/1000))
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

	public TaskMeasurement handleOverviewSearchResponse(
			TimeFrame timeFrame,
			TaskMeasurement taskMeasurement,
			SearchResponse searchResponse) {
	
		// FIXME: Handle case where no data was found (currently causing java.lang.NullPointerException)
		DateHistogram agg = searchResponse.getAggregations().get("timeBuckets");

		// Iterate through each time bucket
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
//					System.out.println("+++ TIME: " + time 
//							+ ", apdex: " + apdexScore 
//							+ ", invocations " + invocations
//							+ ", satisfied " + satisfied
//							+ ", tolerating " + tolerating);
				}
				catch (ArithmeticException e) {
				}
			}
			else	{
				// When handling response need to fill-in for empty (not returned) buckets
				// min_doc_count does not seem to work with DateHistogram.
//				System.out.println("--- TIME: " + time);
			}
			vitalsTimeSequence.add(time);
			vitalsErrors.add((int) errors);
			vitalsCount.add((int) invocations);
		}
		
//		for (DateHistogram.Bucket entry : agg.getBuckets()) {
//			String key = entry.getKey();                // Key
//			Number nkey = entry.getKeyAsNumber();
//			Filters f = entry.getAggregations().get("counters");
//			for (Filters.Bucket bucket : f.getBuckets())	{
//				System.out.println( "key [" + key + "], " 
//						+ "epoch [" + nkey.longValue() + "], "
//						+ bucket.getKey() + " [" + bucket.getDocCount() + "]");
//			}
//		}
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

	private FiltersAggregationBuilder buildLatencyHistogramFilter(TaskConfig taskConfig)	{
        LatencyHistogramRows latencyHistogramRows = new LatencyHistogramRows(
                        taskConfig.getMeasurement().getRttTolerating(), 
                        taskConfig.getMeasurement().getRttFrustrated());
        FiltersAggregationBuilder builder = AggregationBuilders.filters("latencyHistogram");
        for (LatencyHistogramRow row : latencyHistogramRows.asList())	{ // Sort by latency descending
        		if (row.hasUpperLimit())	{
        			builder = builder.filter(row.getLabel(), FilterBuilders.rangeFilter("msecElapsed").gt(row.getLowerLimit()).lte(row.getUpperLimit()));
        		}
        		else	{
        			builder = builder.filter(row.getLabel(), FilterBuilders.rangeFilter("msecElapsed").gt(row.getLowerLimit()));
        		}
        }
		return builder;
	}
	
	public XContentBuilder buildSuccessStatsSearchRequest(
			TaskConfig taskConfig,
			TimeFrame timeFrame) throws IOException	{
		
		long earliest = timeFrame.getEarliest();
		long latest = timeFrame.getLatest();
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
		    .interval(DateHistogram.Interval.seconds(taskConfig.getMeasurement().getSnap()/1000))
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
		    	            	.subAggregation(buildLatencyHistogramFilter(taskConfig)
		    		));

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
			TaskConfig taskConfig, 
			TimeFrame timeFrame,
			TaskMeasurement taskMeasurement,
			SearchResponse searchResponse) {
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
//				System.out.println("Max [" + stats.getMax() + "]");
				max.add(round(stats.getMax(),0));
				
				Percentiles percentiles = satisfiedBucket.getAggregations().get("percentiles");
				
				for (Percentile entry : percentiles) {
				    double percent = entry.getPercent();    // Percent
				    if (percent == 95.0)	{
                        // Populate p95
				    	double value = entry.getValue();        // Value
//				    	System.out.println("percent [" + percent + "], value [" +  value + "]");
				    	p95.add(round(value,0));
				    }
				}
			
				Filters latencyHistogram = satisfiedBucket.getAggregations().get("latencyHistogram");
                // Populate latencyHistogram
				int i = 0;
				for (LatencyHistogramRow row : latencyHistogramRows.asList())	{ // Sort by latency descending
					// latency histogram count
					int binCount = count.get(i);
					binCount = (int) (Integer.valueOf(binCount) + latencyHistogram.getBucketByKey(row.getLabel()).getDocCount());
					count.set(i, binCount);
					i++;
//					System.out.println("latencyHistogram [" + row.getLabel() // 0-100
//							+ "], value [" +  latencyHistogram.getBucketByKey(row.getLabel()).getDocCount() + "]");
				}
			}
			else	{
				p95.add(null);
				max.add(null);
			}
		}
		// Add VitalsTimechart data
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