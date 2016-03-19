package com.apm4all.tracy;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.elasticsearch.ElasticsearchConstants;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.junit.Test;

import com.apm4all.tracy.apimodel.TaskConfig;
import com.apm4all.tracy.util.TimeFrame;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EsQuerySamples {

	@Test
	public void testSuccessStatsQuery() throws IOException {
		TaskConfig taskConfig = new TaskConfig("myApplication", "myTask");
		TimeFrame timeFrame = new TimeFrame(null, null, null, taskConfig);
		
		System.out.println(timeFrame);
		long earliest = timeFrame.getEarliest();
		long latest = timeFrame.getLatest();
		String taskDefiningFilter = taskConfig.getDefiningFilter();
		BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("@timestamp").gt(earliest).lt(latest))
				.must(QueryBuilders.queryStringQuery(taskDefiningFilter));
		// "aggs" date histogram aggregation
		@SuppressWarnings("rawtypes")
		AggregationBuilder aggregationBuilder1 = AggregationBuilders
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
		    				.filter(">300", FilterBuilders.rangeFilter("msecElapsed").gt(300))
		    		));
		
        XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject().field("query");
        queryBuilder1.toXContent(contentBuilder, null);
        contentBuilder.startObject("aggs");
        aggregationBuilder1.toXContent(contentBuilder, null);
        contentBuilder.endObject();
        contentBuilder.field("size", 0);
        contentBuilder.endObject();
        System.out.println( contentBuilder.string());
	}

	@Test
	public void testFindTaskConfig() throws IOException {
//		https://github.com/apache/camel/blob/camel-2.16.x/components/camel-elasticsearch/src/test/java/org/apache/camel/component/elasticsearch/ElasticsearchComponentTest.java#L161-L177		String taskDefiningFilter = "application:\"demo\" AND task:\"hello-tracy-sim\"";
//		BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery()
//				.must(QueryBuilders.queryStringQuery(taskDefiningFilter));
//		
//        XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject().field("query");
//        queryBuilder1.toXContent(contentBuilder, null);
//        contentBuilder.endObject();
//        System.out.println( contentBuilder.string());
//        SearchResponse response = null;
//        response.getHits().getAt(0).getSourceAsString();
	}	
	
	
}
