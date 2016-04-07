package com.apm4all.tracy.backend;

import com.apm4all.tracy.apimodel.TaskConfig;
import org.apache.camel.*;
import org.apache.camel.component.elasticsearch.ElasticsearchConstants;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EsTaskConfig {
    private ProducerTemplate template;

    public void setTemplate(ProducerTemplate template) {
        this.template = template;
    }

    // TODO: Throw custom exceptions: ESQueryBuildingException ESResponseMarshallingException
    public TaskConfig getTaskConfigFromEs(String application, String task) throws IOException {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(ElasticsearchConstants.PARAM_INDEX_NAME, "entities");
        headers.put(ElasticsearchConstants.PARAM_INDEX_TYPE, "TaskConfig");
        String filter = "application:\"" + application + "\" AND task:\"" + task + "\"";
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.queryStringQuery(filter));
        XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject().field("query");
        queryBuilder.toXContent(contentBuilder, null);
        contentBuilder.endObject();
//		System.out.println("=== Built query: " +  contentBuilder.string());
        SearchResponse response = template.requestBodyAndHeaders("elasticsearch://local?operation=SEARCH", contentBuilder, headers, SearchResponse.class);
        // Handle response here
        TaskConfig taskConfig;
        if (response.getHits().getTotalHits() > 0)	{
            String responseString = response.getHits().getAt(0).getSourceAsString();
//        System.out.println("=== " +responseString);
            ObjectMapper mapper = new ObjectMapper();
            taskConfig = mapper.readValue(responseString, TaskConfig.class);
        }
        else	{
            taskConfig = null;
        }
        return taskConfig;
    }

    public void getTaskConfig(
            Exchange exchange,
            @Header(com.apm4all.tracy.apimodel.Headers.APPLICATION) String application,
            @Header(com.apm4all.tracy.apimodel.Headers.TASK) String task
    ) throws IOException {
        TaskConfig taskConfig = getTaskConfigFromEs(application, task);
        if (taskConfig != null)	{
            // TODO: Better to use an exception here
            exchange.getIn().setBody(taskConfig);
        }
        else	{
            // TODO: Add standardized error response and set httpStatus
            exchange.getIn().setBody("not found");
        }

    }

    public void setTaskConfig(
            Exchange exchange,
            @Body TaskConfig taskConfig,
            @Header(com.apm4all.tracy.apimodel.Headers.APPLICATION) String application,
            @Header(com.apm4all.tracy.apimodel.Headers.TASK) String task,
            @Headers Map<String, Object> headers)
            throws JsonProcessingException	{
//		System.out.println("*** setTaskConfig ***");
        ObjectMapper mapper = new ObjectMapper();
        String taskConfigAsJson = mapper.writeValueAsString(taskConfig);
        // TODO: Handle JsonProcessingException, define error message and set http status
        headers.put(ElasticsearchConstants.PARAM_INDEX_NAME, "entities");
        headers.put(ElasticsearchConstants.PARAM_INDEX_TYPE, "TaskConfig");
        headers.put(ElasticsearchConstants.PARAM_INDEX_ID, "TaskConfig");
        template.requestBodyAndHeaders("elasticsearch://local?operation=INDEX", taskConfigAsJson, headers);
    }
}
