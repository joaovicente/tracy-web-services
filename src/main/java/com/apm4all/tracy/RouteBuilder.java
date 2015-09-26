package com.apm4all.tracy;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.elasticsearch.ElasticsearchConfiguration;
import org.apache.camel.component.restlet.RestletConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spring.SpringRouteBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.Response;
import org.restlet.data.Status;

public class RouteBuilder extends SpringRouteBuilder {
	
	@Override
	public void configure() throws Exception {
        String measurement =  
                "{"
                + "'dapdexTimechart':{"
                	+ "'timeSequence':[1439312400000,1439312401900,1439312403800,1439312405700,1439312407600,1439312409500,1439312411400,1439312413300,1439312415200,1439312417100,1439312419000,1439312420900,1439312422800,1439312424700,1439312426600,1439312428500],"
                	+ "'dapdexScores':[0.99,0.98,0.99,0.93,0.94,0.97,0.95,0.97,0.83,0.93,0.97,0.94,0.96,0.98,0.95,0.96]"
                + "},"
                + "'vitalsTimechart\":{"
                	+ "'timeSequence':[1439312400000,1439312401900,1439312403800,1439312405700,1439312407600,1439312409500,1439312411400,1439312413300,1439312415200,1439312417100,1439312419000,1439312420900,1439312422800,1439312424700,1439312426600,1439312428500],"
                	+ "'count':[200,243,254,234,253,265,245,247,765,243,265,273,247,256,236,245],"
                	+ "'errors':[1,2,1,2,1,2,1,2,4,1,2,1,2,1,2,1],"
                	+ "'p95':[110,132,141,143,151,134,123,131,111,125,123,143,122,156,116,145]},"
                + "'latencyHistogram':{"
                	+ "'bins':['1200','1140-1200','1080-1139','1020-1079','960-1019','900-959','840-899','780-839','720-779','660-719','600-659','540-599','480-539','420-479','360-419','300-359','240-299','180-239','120-179','60-119','0-59'],"
                	+ "'rttZone':['Frustrated','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Satisfied','Satisfied','Satisfied','Satisfied','Satisfied'],"
                	+ "'count':[4,8,22,22,22,22,76,89,44,134,134,134,178,178,268,447,670,1548,312,89,44]}"
                + "}";

		
        // configure we want to use servlet as the component for the rest DSL
        // and we enable json binding mode
        //restConfiguration().component("servlet")
        restConfiguration().component("servlet").bindingMode(RestBindingMode.auto)
        // and output using pretty print
        .dataFormatProperty("prettyPrint", "true")
        // setup context path and port number that Apache Tomcat will deploy
        // this application with, as we use the servlet component, then we
        // need to aid Camel to tell it these details so Camel knows the url
        // to the REST services.
        // Notice: This is optional, but needed if the RestRegistry should
        // enlist accurate information. You can access the RestRegistry
        // from JMX at runtime
        .contextPath("camel-example-servlet-rest-tomcat/rest").port(8080)
        .enableCORS(true);
        rest("/v1")
            .consumes("application/json").produces("application/json")
            .get("/applications/{application}/tasks/{task}/measurement").description("Get measurement for a Task")
            	.to("bean:taskMeasurementService?method=getTaskMeasurement(${header.application}, ${header.task})")
//                .to("direct:fakeTaskMeasurement");
            .get("/applications").description("Get Tasks")
            	.to("bean:applicationsService?method=getApplications()");
//                .to("direct:fakeTaskMeasurement");

        from("direct:fakeTaskMeasurement")
            .setBody(simple(measurement));
		
		from("restlet:http://localhost:8050/tracy/segment?restletMethod=POST")
			// Tracy publishing should never block the sender
			// waitForTaskToComplete allows endpoint to respond
			// without having to wait until tracy is finally stored
			.to("seda:tracySegmentProcessor?waitForTaskToComplete=Never")
			// Return taskId-component as reference with HTTP 202 code (Accepted)
			.setBody(simple("{\"status\":\"processing\"}"))
			.setHeader("Access-Control-Allow-Origin", simple("*"))
			.process(new Processor()	{
				@Override
				public void process(Exchange exchange) throws Exception {
					Response response = exchange.getIn().getHeader(RestletConstants.RESTLET_RESPONSE, Response.class);
		            response.setStatus(Status.SUCCESS_ACCEPTED);
				}
			});
		
		from("seda:tracySegmentProcessor")
			.unmarshal().json(JsonLibrary.Jackson)
//			.transform().simple("${body[tracySegment]}")
			// TODO: Validate tracySegment messages
			// TODO: Send invalid segments to audit log
			.split(body())
			.to("seda:storeTracy");
	
		
		// TODO: POST tracy-2015.04.22/webapp1/AAAAAAAAAAAAAAAAAAA001-O001
//		{
//		    "taskId": "AAAAAAAAAAAAAAAAAAA001",
//		    "optId": "O001",
//		    "msecBefore": 1429680861000,
//		    "@timestamp": "2015-04-22T06:34:12.000",
//		    "component":"webapp1",
//		    "label": "manual"
//		}
//		from("seda:storeTracy")
//			// Store Tracy frames in repository
//			.process(new Processor()	{
//				@Override
//				public void process(Exchange exchange) throws Exception {
//					@SuppressWarnings("unchecked")
//					Map<String, Object> tracyMap = (Map<String, Object>) exchange.getIn().getBody();
//					String indexId = tracyMap.get("taskId") + "_" + tracyMap.get("optId");
//					
//					// "_id": "<taskId>_<optId>",
//					exchange.getOut().setHeader("indexId", indexId);
//					
//					//TODO: Move  DateTimeZone.setDefault(DateTimeZone.UTC); to where it is only called once
//					DateTimeZone.setDefault(DateTimeZone.UTC);
//					// "@timestamp": "2015-03-10T23:33:27.707",
//					Long msecBefore = (Long) tracyMap.get("msecBefore");
//					DateTime dateTime = new DateTime(msecBefore);
//					String timestamp = dateTime.toString("yyyy-MM-dd'T'HH:mm:ss.SSS");
//					tracyMap.put("@timestamp", timestamp);
//					
//					// "_index": "tracy-2015.03.10",
//					String index = "tracy" + "-" + dateTime.toString("yyyy.MM.dd");
//					
//		            exchange.getOut().setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME, index);
//		            exchange.getOut().setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE, "taskType1");
//					
//					exchange.getOut().setBody(tracyMap);
//				}
//			})
//			.to("elasticsearch://local?operation=INDEX");
	}
}
