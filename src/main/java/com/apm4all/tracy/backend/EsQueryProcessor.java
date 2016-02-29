package com.apm4all.tracy.backend;

import java.io.IOException;

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

import com.apm4all.tracy.apimodel.TaskConfig;
import com.apm4all.tracy.apimodel.TaskMeasurement;

public class EsQueryProcessor {
	public XContentBuilder buildOverviewSearchRequest(TaskConfig taskConfig) throws IOException	{
		// "bool" Restrict results by time range and match criteria
		if (null == taskConfig)	{
			taskConfig = new TaskConfig();
		}
		long now = System.currentTimeMillis();
		long latest = now - now % taskConfig.getMeasurement().getSnap(); 
		long earliest = latest - taskConfig.getMeasurement().getSpan(); // last 15 minutes
		int rttTolerating = taskConfig.getMeasurement().getRttTolerating();
		int rttFrustrated = taskConfig.getMeasurement().getRttFrustrated();
		String definingFilter = taskConfig.getDefiningFilter();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				// TODO: For improved performance, filtering should be done at index level as well to avoid accessing uneccessary indexes
				.must(QueryBuilders.rangeQuery("@timestamp").gt(earliest).lt(latest))
				.must(QueryBuilders.simpleQueryStringQuery(definingFilter));
//				.queryString("\"component\":\"hello-tracy\"").analyzeWildcard(true);
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
		    	            .filter("invocations", FilterBuilders.matchAllFilter())
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
	
	public TaskMeasurement handleOverviewSearchResponse(TaskConfig taskConfig, TaskMeasurement taskMeasurement, 
			SearchResponse searchResponse)	{
		if (null == taskConfig)	{
			taskConfig = new TaskConfig();
		}
		DateHistogram agg = searchResponse.getAggregations().get("timeBuckets");
		// For each entry
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
		// TODO: Populate TaskMeasurement with Overview SearchResponse
		return taskMeasurement;
	}
}
