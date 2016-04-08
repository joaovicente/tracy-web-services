package com.apm4all.tracy.backend;

import com.apm4all.tracy.apimodel.TaskAnalysis;
import com.apm4all.tracy.apimodel.TaskConfig;
import com.apm4all.tracy.simulations.TaskAnalysisFake;
import com.apm4all.tracy.util.TimeFrame;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Headers;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.elasticsearch.ElasticsearchConstants;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.apm4all.tracy.apimodel.Headers.*;

public class EsTaskAnalysis {
    private ProducerTemplate template;
    private EsTaskConfig esTaskConfig;

    public void setTemplate(ProducerTemplate template) {
        this.template = template;
    }

    public void setEsTaskConfig(EsTaskConfig esTaskConfig)	{
        this.esTaskConfig = esTaskConfig;
    }

    public TaskAnalysis getTaskAnalysis(
            Exchange exchange,
            @Header(APPLICATION) String application,
            @Header(TASK) String task,
            @Header(EARLIEST) String earliest,
            @Header(LATEST) String latest,
            @Header(FILTER) String filter,
            @Header(SORT) String sort,
            @Header(OFFSET) String offset,
            @Header(LIMIT) String limit,
            @Headers Map<String, Object> headers) throws IOException {
        TaskAnalysis taskAnalysis = null;
        int limitInt = Integer.parseInt(limit);
        int offsetInt = Integer.parseInt(offset);

        // Set ES headers required for all queries
        headers.put(ElasticsearchConstants.PARAM_INDEX_NAME, "tracy*");
        headers.put(ElasticsearchConstants.PARAM_INDEX_TYPE, "tracy");

        System.out.println("=== headers: " + headers.toString());

        if (!headers.containsKey("mock")) {
            TaskConfig taskConfig = esTaskConfig.getTaskConfigFromEs(application, task);
            TimeFrame timeFrame = new TimeFrame(earliest, latest, taskConfig);
            // Get list of (20) taskIds for search criteria (see EsQueryProcessor getTaskConfigFromEs)
            List<String> taskIds = getTaskIdsMatchingCriteria(taskConfig, timeFrame, filter, sort, limitInt, offsetInt, headers);
            // Get Tracy events for each taskId
            Map<String, List<Object>> tracyEventsMap = getTracyForTaskIds(taskIds, timeFrame, headers);
            System.out.println("== tracyEventsMap:" + tracyEventsMap.toString());
            // TODO: Fill in TaskAnalysis
            RetrievedTaskAnalysis retrievedTaskAnalysis =
                    new RetrievedTaskAnalysis(application, task, timeFrame.getEarliest(),
                            timeFrame.getLatest(), filter, sort, limitInt, offsetInt);
            retrievedTaskAnalysis.setTracyTasks(taskIds, tracyEventsMap);
            taskAnalysis = retrievedTaskAnalysis;
        }
        else    {
            taskAnalysis = new TaskAnalysisFake(application, task, Long.parseLong(earliest),
                    Long.parseLong(latest), filter, sort, limitInt, offsetInt);
        }



        return taskAnalysis;
    }

    // TODO: Throw custom exceptions: ESQueryBuildingException ESResponseMarshallingException
    public List<String> getTaskIdsMatchingCriteria(
            TaskConfig taskConfig,
            TimeFrame timeFrame,
            String filter,
            String sort,
            int limit,
            int offset,
            Map<String, Object> headers)
            throws IOException {

        StringBuilder finalFilter = new StringBuilder();
        // TODO: if the filter is not in msec then conversion from unit msec is required
        finalFilter.append("(")
                .append(taskConfig.getDefiningFilter())
                .append(") AND ")
                .append(filter);

        long earliest = timeFrame.getEarliest();
        long latest = timeFrame.getLatest();
        String analysisFilter = finalFilter.toString();

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp").gte(earliest).lte(latest))
                .must(QueryBuilders.queryStringQuery(analysisFilter));

        XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject().field("query");
        queryBuilder.toXContent(contentBuilder, null);
        contentBuilder.field("size", limit);

        // "sort": [ { "msecElapsed" : {"unmapped_type" : "long"} } ]
        HashMap<String, String> unmappedType = new HashMap<String, String>();
        unmappedType.put("unmapped_type", "long");
        HashMap<String, Map> msecElapsed = new HashMap<String, Map>();
        msecElapsed.put("msecElapsed", unmappedType);
        ArrayList sortList = new ArrayList();
        sortList.add(msecElapsed);
        contentBuilder.field("sort", sortList);

        //"fields": ["taskId"]
        ArrayList fieldsArray = new ArrayList();
        fieldsArray.add("taskId");
        contentBuilder.field("fields", fieldsArray);

        contentBuilder.endObject();

		System.out.println("=== Built query: " +  contentBuilder.string());
        SearchResponse response = template.requestBodyAndHeaders("elasticsearch://local?operation=SEARCH", contentBuilder, headers, SearchResponse.class);

        System.out.println("===  response =" + response.toString());

        ArrayList<String> taskIds = new ArrayList<String>();
        // Handle response

        long hitCount = Math.min(response.getHits().getTotalHits(), limit);
        System.out.println("===  hitCount: " + hitCount);
        for (int i=0 ; i < hitCount ; i++)   {
            String taskId = response.getHits().getAt(i).getFields().get("taskId").getValues().get(0).toString();
            taskIds.add(taskId);
            System.out.println("===  taskId =" + taskId );
        }
        return taskIds;
    }

    private Map<String, List<Object>> getTracyForTaskIds(
            List<String> taskIds,
            TimeFrame timeFrame,
            Map<String, Object> headers ) throws IOException
    {
        // A number way beyond the maximum number of events returned
        // To ensure the response returns all Tracy events for tasks requested
        final int MAX_TRACY_EVENTS = 1000;

        // Build query string
        StringBuilder sb = new StringBuilder();
        boolean firstTaskId = true;
        for (String taskId : taskIds)   {
            if (firstTaskId == false)    {
                sb.append(" OR ");
            }
            sb.append("taskId:\"").append(taskId).append("\"");
            firstTaskId = false;
        }
        String taskDefiningFilter = sb.toString();

        // Build query
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp").gte(timeFrame.getEarliest()).lte(timeFrame.getLatest()))
                .must(QueryBuilders.queryStringQuery(taskDefiningFilter));
        XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject().field("query");
        queryBuilder.toXContent(contentBuilder, null);
        contentBuilder.field("size", MAX_TRACY_EVENTS); // large number of Tracy events we
        contentBuilder.endObject();
        System.out.println("== Analysis get tracy events query:" + contentBuilder.string());

        SearchResponse response = template.requestBodyAndHeaders("elasticsearch://local?operation=SEARCH", contentBuilder, headers, SearchResponse.class);

        // Put all Tracy events in a Map keyed by taskId and containing list of taskId Tracy events
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, List<Object>> tracyEventsMap = new HashMap<>();
        for(int i=0; i < response.getHits().getTotalHits() ; i++)   {
            String tracyEventAsString = response.getHits().getAt(i).getSourceAsString();
            JsonNode jsonNode = objectMapper.readTree(tracyEventAsString);

            Map tracyAsMap = objectMapper.readValue(tracyEventAsString, Map.class);

            String taskId = jsonNode.get("taskId").textValue();
            List<Object> tracyEventList;
            if (!tracyEventsMap.containsKey(taskId))    {
                tracyEventList = new ArrayList<>();
                tracyEventsMap.put(taskId, tracyEventList);
            }
            else    {
                tracyEventList = tracyEventsMap.get(taskId);
            }
            tracyEventList.add(tracyAsMap);
        }
        return tracyEventsMap;
    }
}
