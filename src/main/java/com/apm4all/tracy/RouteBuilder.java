package com.apm4all.tracy;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.elasticsearch.ElasticsearchConfiguration;
import org.apache.camel.component.restlet.RestletConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.SpringRouteBuilder;
import org.joda.time.DateTime;
import org.restlet.Response;
import org.restlet.data.Status;

public class RouteBuilder extends SpringRouteBuilder {
	
	@Override
	public void configure() throws Exception {
		from("restlet:http://localhost:8050/tracy/segment?restletMethod=POST")
			// Tracy publishing should never block the sender
			// waitForTaskToComplete allows endpoint to respond
			// without having to wait until tracy is finally stored
			.to("seda:tracySegmentProcessor?waitForTaskToComplete=Never")
			// Return taskId-component as reference with HTTP 202 code (Accepted)
			.setBody(simple("{\"status\":\"processing\"}"))
			.process(new Processor()	{
				@Override
				public void process(Exchange exchange) throws Exception {
					Response response = exchange.getIn().getHeader(RestletConstants.RESTLET_RESPONSE, Response.class);
		            response.setStatus(Status.SUCCESS_ACCEPTED);
				}
			});
		
		from("seda:tracySegmentProcessor")
			.unmarshal().json(JsonLibrary.Jackson)
			.transform().simple("${body[tracySegment]}")
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
		from("seda:storeTracy")
			// TODO: Store Tracy frames in repository
			.process(new Processor()	{
				@Override
				public void process(Exchange exchange) throws Exception {
					@SuppressWarnings("unchecked")
					Map<String, Object> tracyMap = (Map<String, Object>) exchange.getIn().getBody();
					String indexId = tracyMap.get("taskId") + "_" + tracyMap.get("optId");
					
					// "_id": "<taskId>_<optId>",
					exchange.getOut().setHeader("indexId", indexId);

					// "@timestamp": "2015-03-10T23:33:27.707Z",
					Long msecBefore = (Long) tracyMap.get("msecBefore");
					DateTime dateTime = new DateTime(msecBefore);
					String timestamp = dateTime.toString("yyyy-MM-dd'T'HH:mm:ss.SSS");
					tracyMap.put("@timestamp", timestamp);
					
					// "_index": "tracy-2015.03.10",
					String index = "tracy" + "-" + dateTime.toString("yyyy.MM.dd");
					
		            exchange.getOut().setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME, index);
		            exchange.getOut().setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE, "taskType1");
					
					exchange.getOut().setBody(tracyMap);
				}
			})
			.to("elasticsearch://local?operation=INDEX");
	}
}
