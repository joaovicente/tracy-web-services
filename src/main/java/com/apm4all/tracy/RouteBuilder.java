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

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.elasticsearch.ElasticsearchConstants;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.processor.interceptor.DefaultTraceFormatter;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.camel.spring.SpringRouteBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.apm4all.tracy.apimodel.ApplicationMeasurement;
import com.apm4all.tracy.apimodel.TaskConfig;
import com.apm4all.tracy.apimodel.TaskMeasurement;
import com.apm4all.tracy.simulations.TaskAnalysisFake;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class RouteBuilder extends SpringRouteBuilder {

	private boolean tracySimulationEnabled = false;
	private boolean flushTracy = false; // DONT Flush tracy at start-up
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
            	.to("direct:taskMeasurement")

            .get("/applications/{application}/measurement").description("Get measurement for an Application").outType(ApplicationMeasurement.class)
              .param().name("application").type(path).description("The application to measure").dataType("string").endParam()
            	.to("bean:applicationMeasurementService?method=getApplicationMeasurement(${header.application})")

            .post("/applications/{application}/tasks/{task}/config").description("Set Task config").type(TaskConfig.class)
              .param().name("application").type(path).description("The application").dataType("string").endParam()
              .param().name("task").type(path).description("The task").dataType("string").endParam()
                .to("bean:esTaskConfig?method=setTaskConfig")

            .get("/applications/{application}/tasks/{task}/config").description("Get Task config").outType(TaskConfig.class)
              .param().name("application").type(path).description("The application").dataType("string").endParam()
              .param().name("task").type(path).description("The task").dataType("string").endParam()
                .to("bean:esTaskConfig?method=getTaskConfig")

			.options("/applications/{application}/tasks/{task}/config")
				.to("direct:trash")

			.get("/registry").description("Get Tracy Registry containing supported environments")
				.to("direct:registry")

			.get("/capabilities").description("Get Server capabilities (Applications/Tasks supported and associated views)")
				.to("direct:capabilities")

            .get("/applications/{application}/tasks/{task}/analysis").description("Get analysis for a Task").outType(TaskAnalysisFake.class)
              .param().name("application").type(path).description("The application to analyse").dataType("string").endParam()
              .param().name("task").type(path).description("The task to analyse").dataType("string").endParam()
              .param().name("earliest").type(query).description("The earliest time (in epoch msec)").dataType("integer").endParam()
              .param().name("latest").type(query).description("The latest time (in epoch msec)").dataType("integer").endParam()
              .param().name("filter").type(query).description("The expression to filter analysis").dataType("string").endParam()
              .param().name("sort").type(query).description("The fields to sort by").dataType("string").endParam()
              .param().name("limit").type(query).defaultValue("20").description("The number of records to analyse, i.e. page size, default is 20").dataType("integer").endParam()
              .param().name("offset").type(query).description("The page number").defaultValue("1").dataType("integer").endParam()
                .to("direct:taskAnalysis")

			.delete("/tracy").description("Delete all Tracy events stored in backed")
				.to("direct:flushTracyRequest")

            .post("/tracySimulation").description("Produce Tracy for simulation purposes")
               .to("direct:toogleTracySimulation")

			.get("/demo")
				.to("direct:getSimulation")

			.post("/demo")
				.to("direct:setSimulation");


		from("direct:trash").stop();

		from("direct:getSimulation").routeId("getSimulation")
				.setBody(simple(""))
				.process(new Processor()	{
					@Override
					public void process(Exchange exchange) throws Exception {
						Map<String,Boolean> state = new HashMap<String,Boolean>();
						state.put("demo", tracySimulationEnabled);
						exchange.getIn().setBody(state);
					}
				});

		from("direct:setSimulation").routeId("setSimulation")
	            .log("${body}")
				.process(new Processor()	{
					@Override
					public void process(Exchange exchange) throws Exception {
						Map<String,Boolean> state = (Map<String, Boolean>) exchange.getIn().getBody();
						tracySimulationEnabled = state.get("demo");
						state.put("demo", tracySimulationEnabled);
						exchange.getIn().setBody(state);
					}
				});

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
          .to("seda:flushTracy")
          .choice()
            .when(simple("${in.header.TRACY_SIMULATION_ENABLED} == true"))
//              .loop(100).to("seda:generateTracy")
				.to("seda:generateTracy") // To not loop
                .end();

        from("seda:generateTracy").routeId("generateTracy")
          .setBody(simple(""))
          .process(new Processor()	{
				@Override
				public void process(Exchange exchange) throws Exception {
					//TODO: Extract Tracy generation to a separate thread
					final String COMPONENT = "hello-tracy";
					final String OUTER = "serviceEndpoint";
					final String INNER = "dodgyBackend";
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
//					long delayInMsec = new Double(Math.random() * 2).longValue() + 2;
					long delayInMsec = new Double(Math.random() * 200).longValue() + 100;
		        	Thread.sleep(delayInMsec);
		    		Tracy.after(INNER);
//					delayInMsec = new Double(Math.random() * 2).longValue() + 2;
					delayInMsec = new Double(Math.random() * 10).longValue() + 10;
		        	Thread.sleep(delayInMsec);
		    		Tracy.after(OUTER);
					exchange.getIn().setBody(Tracy.getEventsAsJson());
		    		Tracy.clearContext();
				}
			})
			.to("seda:ingestTracy");

        from("direct:flushTracyRequest").routeId("flushTracyRequest")
            .process(new Processor()	{
                @Override
                public void process(Exchange exchange) throws Exception {
                    flushTracy = true;
                }
            })
            .setBody(simple("Flushed all Tracy events"))
            .log("Flush request accepted");

        from("seda:flushTracy").routeId("flushTracy")
//            .log("Flush request processing started")
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
					exchange.getIn().setBody("");
				}
			})
			.setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.DELETE))
//            .log("Flush request ready to be sent")
			.choice()
			  .when(simple("${in.header.FLUSH_TRACY} == true"))
                //TODO: Hanle 404 status (nothing to delete) gracefully
			    .to("http4://localhost:9200/tracy-*/tracy")
                  //TODO: Investigate why Camel ES Delete is not working
//			      .setHeader(ElasticsearchConstants.PARAM_INDEX_NAME, simple("tracy-hello-tracy-*"))
//                 .setHeader(ElasticsearchConstants.PARAM_INDEX_TYPE, simple("tracy"))
//                .to("elasticsearch://local?operation=DELETE");
                .log("Flush request sent")
            .end();

        from("seda:ingestTracy").routeId("ingestTracy")
          //TODO: If tracySegment instead of tracyFrame, split into Tracy frames (not required for MVC)
          .split(body())
//          .setHeader(ElasticsearchConstants.PARAM_INDEX_NAME, "tracy-" + simple("${body[component]}")
          .process(new Processor()	{
				@Override
				public void process(Exchange exchange) throws Exception {
			        ObjectMapper m = new ObjectMapper();
			        JsonNode rootNode = m.readTree((String)exchange.getIn().getBody());
					DateTime dt = new DateTime(rootNode.path("msecBefore").asLong(), DateTimeZone.UTC);
					String esTimestamp = dt.toString("yyyy-MM-dd'T'HH:mm:ss.SSS");
					((ObjectNode) rootNode).put("@timestamp", esTimestamp);
					StringBuilder index = new StringBuilder();
					DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy.MM.dd");
					String dateString = fmt.print(dt);
					index.append("tracy-").append(rootNode.path("component").textValue())
						.append("-").append(dateString);
					exchange.getIn().setHeader(ElasticsearchConstants.PARAM_INDEX_NAME, index.toString());
					exchange.getIn().setHeader(ElasticsearchConstants.PARAM_INDEX_TYPE, "tracy");
					String indexId = rootNode.path("taskId").textValue() + "_" + rootNode.path("optId").textValue();
					exchange.getIn().setHeader(ElasticsearchConstants.PARAM_INDEX_ID, indexId);
					exchange.getIn().setBody(m.writer().writeValueAsString(rootNode));
				}
			})
//          .log("${body}")
//          .log("${headers}")
		  .to("elasticsearch://local?operation=INDEX");

		from("direct:registry").routeId("registry")
				.process(new Processor()	{
					@Override
					public void process(Exchange exchange) throws Exception {
						ObjectMapper m = new ObjectMapper();
						Map<String,Object> registry = m.readValue(
                                "{\"environments\":[{\"name\":\"Local1\",\"servers\":[{\"url\":\"http://localhost:8080/tws/v1\"}]},{\"name\":\"Local2\",\"servers\":[{\"url\":\"http://localhost:8080/tws/v1\"}]}]}",
								Map.class);
						exchange.getIn().setBody(registry);
					}
				});

		from("direct:capabilities").routeId("capabilities")
				.process(new Processor()	{
					@Override
					public void process(Exchange exchange) throws Exception {
						ObjectMapper m = new ObjectMapper();
						Map<String,Object> capabilities = m.readValue(
								"{\"capabilities\":{\"applications\":[{\"name\":\"appX\",\"views\":[{\"label\":\"Measurement\",\"name\":\"measurement\"}],\"tasks\":[{\"name\":\"taskX1\",\"views\":[{\"label\":\"Measurement\",\"name\":\"measurement\"}]}]}]}}",
								Map.class);
						exchange.getIn().setBody(capabilities);
					}
				});


        from("direct:taskMeasurement").routeId("taskMeasurement")
                .choice()
                    .when(simple("${in.header.application} contains 'demo-live'"))
                        .bean("esTaskMeasurement", "getTaskMeasurement")
                    .when(simple("${in.header.application} contains 'demo-static'"))
                        .to("bean:taskMeasurementService?method=getTaskMeasurement(${header.application}, ${header.task})")
                .end();

        from("direct:taskAnalysis").routeId("taskAnalysis")
//                .log("${headers}")
                .choice()
                    .when(simple("${in.header.application} contains 'demo-live'"))
                        .bean("esTaskAnalysis", "getTaskAnalysis")
                    .when(simple("${in.header.application} contains 'demo-static'"))
                        .to("bean:taskAnalysisService?method=getTaskAnalysis" +
                                "(${header.application}, ${header.task}, ${header.earliest}, ${header.latest}, ${header.filter}, ${header.sort}, ${header.limit}, ${header.offset})")
                .end();
	}
}
