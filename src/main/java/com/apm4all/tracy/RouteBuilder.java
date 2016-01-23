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
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.elasticsearch.ElasticsearchConfiguration;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spring.SpringRouteBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import com.apm4all.tracy.measurement.task.TaskMeasurement;

import static org.apache.camel.model.rest.RestParamType.body;
import static org.apache.camel.model.rest.RestParamType.path;

public class RouteBuilder extends SpringRouteBuilder {
	
	@Override
	public void configure() throws Exception {

        // configure we want to use servlet as the component for the rest DSL
        // and we enable json binding mode //netty4-http
        restConfiguration().component("servlet").bindingMode(RestBindingMode.json)
            // and output using pretty print
            .dataFormatProperty("prettyPrint", "true")
            // setup context path and port number that netty will use
            .contextPath("tws/v1").port(8080)
            // add swagger api-doc out of the box
            .apiContextPath("/api-doc")
                .apiProperty("api.title", "Tracy Web Services API").apiProperty("api.version", "1.0.0")
                // and enable CORS
                .apiProperty("cors", "true");

        rest("/v1").description("Tracy Web Service")
            .consumes("application/json").produces("application/json")
            .get("/applications/{application}/tasks/{task}/measurement").description("Get measurement for an Application/Task").outType(TaskMeasurement.class)
              .param().name("application").type(path).description("The application to measure").dataType("string").endParam()
              .param().name("task").type(path).description("The task to measure").dataType("string").endParam()
            	.to("bean:taskMeasurementService?method=getTaskMeasurement(${header.application}, ${header.task})");
            	
// ******************************** Start of fix me **************************************** 

//            .get("/applications/{application}/measurement").description("Get measurement for an Application")
//            	.to("bean:applicationMeasurementService?method=getApplicationMeasurement(${header.application})")

//            .get("/applications").description("Get Application and Task hierarchy")
//            	.to("bean:applicationsService?method=getApplications()")
        
//            .get("/applications/{application}/tasks/{task}/analysis").description("Get analysis for a Task")
//            	.to("bean:taskAnalysisService?method=getTaskAnalysis(${header.application}, ${header.task}, ${header.earliest}, ${header.latest}, ${header.filter}, ${header.sort}, ${header.limit}, ${header.offset})");

// ******************************** End of fix me **************************************** 

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
