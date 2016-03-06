/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apm4all.tracy;
import static org.apache.camel.model.rest.RestParamType.path;
import static org.apache.camel.model.rest.RestParamType.query;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.elasticsearch.ElasticsearchConstants;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.processor.interceptor.DefaultTraceFormatter;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.camel.spring.SpringRouteBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.apm4all.tracy.apimodel.ApplicationMeasurement;
import com.apm4all.tracy.apimodel.TaskMeasurement;
import com.apm4all.tracy.simulations.TaskAnalysisFake;

public class RouteBuilder extends SpringRouteBuilder {

	private boolean tracySimulationEnabled = false; 
	private boolean flushTracy = true; // Flush tracy at start-up
	static final String TRACY_SIMULATION_ENABLED = "TRACY_SIMULATION_ENABLED";
	static final String FLUSH_TRACY = "FLUSH_TRACY";
	
	@Override
	public void configure() throws Exception {
		Tracer tracer = new Tracer();
		tracer.setTraceOutExchanges(true);
		tracer.setEnabled(false);
		 
		// we configure the default trace formatter where we can
		// specify which fields we want in the output
		DefaultTraceFormatter formatter = new DefaultTraceFormatter();
//		formatter.setShowOutBody(true);
//		formatter.setShowOutBodyType(true);
		formatter.setShowBody(true);
		formatter.setShowBodyType(true);
		 
		// set to use our formatter
		tracer.setFormatter(formatter);
		 
		getContext().addInterceptStrategy(tracer);

        // configure we want to use servlet as the component for the rest DSL
        // and we enable json binding mode //netty4-http
        restConfiguration().component("servlet").bindingMode(RestBindingMode.json)
            // and output using pretty print
            .dataFormatProperty("prettyPrint", "true")
            // setup context path and port number that netty will use
            .contextPath("tws").port(8080)
            // add swagger api-doc out of the box
            .apiContextPath("/api-doc")
                .apiProperty("api.title", "Tracy Web Services API").apiProperty("api.version", "1.0.0")
                // and enable CORS
                .apiProperty("cors", "true");

        rest().description("Tracy Web Service")
            .consumes("application/json").produces("application/json")
            .get("/applications/{application}/tasks/{task}/measurement").description("Get measurement for a Task").outType(TaskMeasurement.class)
              .param().name("application").type(path).description("The application to measure").dataType("string").endParam()
              .param().name("task").type(path).description("The task to measure").dataType("string").endParam()
            	.to("bean:taskMeasurementService?method=getTaskMeasurement(${header.application}, ${header.task})")
            
            .get("/applications/{application}/measurement").description("Get measurement for an Application").outType(ApplicationMeasurement.class)
              .param().name("application").type(path).description("The application to measure").dataType("string").endParam()
            	.to("bean:applicationMeasurementService?method=getApplicationMeasurement(${header.application})")            	
            	
            .get("/applications/{application}/tasks/{task}/search").description("Test ES Search").outType(TaskMeasurement.class)
              .param().name("application").type(path).description("The application to measure").dataType("string").endParam()
              .param().name("task").type(path).description("The task to measure").dataType("string").endParam()
            	.to("direct:search")
            	
            .get("/applications/{application}/tasks/{task}/analysis").description("Get analysis for a Task").outType(TaskAnalysisFake.class)
              .param().name("application").type(path).description("The application to analyse").dataType("string").endParam()
              .param().name("task").type(path).description("The task to analyse").dataType("string").endParam()
              .param().name("earliest").type(query).description("The earliest time (in epoch msec)").dataType("integer").endParam()
              .param().name("latest").type(query).description("The latest time (in epoch msec)").dataType("integer").endParam()
              .param().name("filter").type(query).description("The expression to filter analysis").dataType("string").endParam()
              .param().name("sort").type(query).description("The fields to sort by").dataType("string").endParam()
              .param().name("limit").type(query).defaultValue("20").description("The number of records to analyse, i.e. page size, default is 20").dataType("integer").endParam()
              .param().name("offset").type(query).description("The page number").defaultValue("1").dataType("integer").endParam()
            	.to("bean:taskAnalysisService?method=getTaskAnalysis(${header.application}, ${header.task}, ${header.earliest}, ${header.latest}, ${header.filter}, ${header.sort}, ${header.limit}, ${header.offset})")
        
            .post("/tracySimulation").description("Produce Tracy for simulation purposes")
               .to("direct:toogleTracySimulation");
            
             
        from("direct:toogleTracySimulation").routeId("toogleTracySimulation")
          .setBody(simple(""))
          .process(new Processor()	{
				@Override
				public void process(Exchange exchange) throws Exception {
					String response;
					tracySimulationEnabled = !tracySimulationEnabled;
					if(tracySimulationEnabled) {
						response = "Tracy simulation enabled";
					}
					else {
						response = "Tracy simulation disabled";
					}
					exchange.getIn().setBody(response);
				}
			});

        from("quartz://everySecond?cron=0/1+*+*+*+*+?").routeId("everySecondTimer")
          .process(new Processor()	{
				@Override
				public void process(Exchange exchange) throws Exception {
					Map<String, Object> headers = exchange.getIn().getHeaders();
					if (tracySimulationEnabled)	{
					  headers.put(TRACY_SIMULATION_ENABLED, new Boolean(true));
					}
					else {
					  headers.put(TRACY_SIMULATION_ENABLED, new Boolean(false));
					}
				}
			})
          .choice()
            .when(simple("${in.header.TRACY_SIMULATION_ENABLED} == true"))
              .to("seda:generateTracy")
              .to("seda:flushOldTracy");
        
        from("seda:generateTracy").routeId("generateTracy")
          .setBody(simple(""))
          .process(new Processor()	{
				@Override
				public void process(Exchange exchange) throws Exception {
					//TODO: Extract Tracy generation to a separate thread
					final String COMPONENT = "hello-tracy";
					final String OUTER = "outer";
					final String INNER = "inner";
			    	int status = 200;
			    	long random = new Double(Math.random() * 100).longValue()+1;
			    	if      ( random <= 80 )	{ status = 200; }//  80%  200: OK
			    	else if ( random  > 99 ) { status = 202; }//   1%  202: Accepted
			    	else if ( random  > 97 ) { status = 429; }//   1%  307: Temp redirect
			    	else if ( random  > 87 ) { status = 404; }//  10%  404: Not found
			    	else if ( random  > 84 ) { status = 401; }//   3%  401: Unauthorized
			    	else if ( random  > 82 ) { status = 400; }//   2%  404: Bad request
			    	else if ( random  > 81 ) { status = 307; }//   2%  429: Too many requests
			    	else if ( random  > 80 ) { status = 500; }//   1%  500: Internal server error	
		    		Tracy.setContext(null, null, COMPONENT);
		    		Tracy.before(OUTER);
		    		Tracy.annotate("status", status);
		    		Tracy.before(INNER);
		        	long delayInMsec = new Double(Math.random() * 10).longValue() + 10;
		        	Thread.sleep(delayInMsec);
		    		Tracy.after(INNER);
		        	delayInMsec = new Double(Math.random() * 200).longValue() + 100;
		        	Thread.sleep(delayInMsec);
		    		Tracy.after(OUTER);
					exchange.getIn().setBody(Tracy.getEventsAsMaps());
		    		Tracy.clearContext();
				}
			})
			.to("seda:ingestTracy");
        
        from("seda:flushOldTracy").routeId("flushOldTracy")
          //TODO: Prepare older than 60 minutes Tracy events
          .process(new Processor()	{
				@Override
				public void process(Exchange exchange) throws Exception {
					Map<String, Object> headers = exchange.getIn().getHeaders();
					if (flushTracy)	{
						  headers.clear();
						  headers.put(FLUSH_TRACY, new Boolean(true));
						  flushTracy = false;
					}
					else	{
						  headers.clear();
						  headers.put(FLUSH_TRACY, new Boolean(false));
					}
//					exchange.getIn().setBody("");
				}
			})
			.setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.DELETE))
			.choice()
			  .when(simple("${in.header.FLUSH_TRACY} == true"))
                .log("flushing old tracy")
			    .to("http4://localhost:9200/tracy-hello-tracy-*/tracy")
                  //TODO: Investigate why Camel ES Delete is not working 
//			      .setHeader(ElasticsearchConstants.PARAM_INDEX_NAME, simple("tracy-hello-tracy-*"))
//                 .setHeader(ElasticsearchConstants.PARAM_INDEX_TYPE, simple("tracy"))
//                .to("elasticsearch://local?operation=DELETE");
			    .endChoice();
          
        from("seda:ingestTracy").routeId("ingestTracy")
          //TODO: If tracySegment instead of tracyFrame, split into Tracy frames (not required for MVC)
          .split(body())
//          .setHeader(ElasticsearchConstants.PARAM_INDEX_NAME, "tracy-" + simple("${body[component]}")
          .process(new Processor()	{
				@Override
				public void process(Exchange exchange) throws Exception {
					@SuppressWarnings("unchecked")
					Map<String, Object> tracy = (Map<String, Object>) exchange.getIn().getBody(); 
					DateTime dt = new DateTime(Long.parseLong((String) tracy.get("msecBefore")));
					String esTimestamp = dt.toString("yyyy-MM-dd'T'HH:mm:ss.SSS");
					tracy.put("@timestamp", esTimestamp);
					StringBuilder index = new StringBuilder();
					DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy.MM.dd");
					String dateString = fmt.print(dt);
					index.append("tracy-").append(tracy.get("component"))
						.append("-").append(dateString);
					exchange.getIn().setHeader(ElasticsearchConstants.PARAM_INDEX_NAME, index.toString());
					exchange.getIn().setHeader(ElasticsearchConstants.PARAM_INDEX_TYPE, "tracy");
					String indexId = tracy.get("taskId") + "_" + tracy.get("optId");
					exchange.getIn().setHeader(ElasticsearchConstants.PARAM_INDEX_ID, indexId);
				}
			})          
//          .log("${body}")
//          .log("${headers}")
		  .to("elasticsearch://local?operation=INDEX");
        
        from("direct:search").routeId("search")
//          .setBody(simple("{ \"query\": { \"match_all\": {} } }"))
//          .setBody(simple("{ \"query\": { \"match\" : { \"component\" : \"hello-tracy\" } } }"))
//          .log("Request: ${body}")
//          .setHeader(ElasticsearchConstants.PARAM_INDEX_NAME, simple("tracy-hello-tracy-2016.02.19"))
          .setHeader(ElasticsearchConstants.PARAM_INDEX_NAME, simple("tracy-hello-tracy-*"))
          .setHeader(ElasticsearchConstants.PARAM_INDEX_TYPE, simple("tracy"))
          // FIXME: Get non-embedded ElasticSearch configuration working (possibly not working in Camel 2.16)          
//		  .to("elasticsearch://jv?operation=SEARCH&transportAddresses=dockerhost:9300&indexName=a&indexType=a")
          .bean("esQueryProcessor", "initMeasurement")
          // TODO: Ensure taskConfig header is available at this point
          .bean("esQueryProcessor", "buildOverviewSearchRequest") // returns SearchRequest
          .choice()
            .when(simple("${in.header.debug} == true"))
            	.log("searchRequest: ${body.string()}")
            	.end()
		  .to("elasticsearch://local?operation=SEARCH")
          .choice()
            .when(simple("${in.header.debug} == true"))
            	.log("searchResponse: ${body}")
            	.end()
          // handles searchResponse body and populates TASK_MEASUREMENT header
          .bean("esQueryProcessor", "handleOverviewSearchResponse");
          // Process SearchResponse
//          .to("bean:taskMeasurementService?method=getTaskMeasurement(${header.application}, ${header.task})");
//        GET _search
//        {
//        "size": 0,
//           "query": {
//              "filtered": {
//                 "query": {
//                    "query_string": {
//                       "analyze_wildcard": true,
//                       "query": "label:\"inner\""
//                    }
//                 }
//              }
//           },
//           "aggs": {
//              "articles_over_time": {
//                 "date_histogram": {
//                    "field": "@timestamp",
//                    "interval": "1m",
//                    "min_doc_count": 0
//                 }
//              }
//            }
//        }
        
        
        
//		from("restlet:http://localhost:8050/tracy/segment?restletMethod=POST")
			// Tracy publishing should never block the sender
			// waitForTaskToComplete allows endpoint to respond
			// without having to wait until tracy is finally stored
//			.to("seda:tracySegmentProcessor?waitForTaskToComplete=Never")
			// Return taskId-component as reference with HTTP 202 code (Accepted)
//			.setBody(simple("{\"status\":\"processing\"}"))
//			.setHeader("Access-Control-Allow-Origin", simple("*"))
//			.process(new Processor()	{
//				@Override
//				public void process(Exchange exchange) throws Exception {
//					Response response = exchange.getIn().getHeader(RestletConstants.RESTLET_RESPONSE, Response.class);
//		            response.setStatus(Status.SUCCESS_ACCEPTED);
//				}
//			});
		
//		from("seda:tracySegmentProcessor")
//			.unmarshal().json(JsonLibrary.Jackson)
//			.transform().simple("${body[tracySegment]}")
			// TODO: Validate tracySegment messages
			// TODO: Send invalid segments to audit log
//			.split(body())
//			.to("seda:storeTracy");
	
		
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
