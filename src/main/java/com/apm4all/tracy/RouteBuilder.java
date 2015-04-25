package com.apm4all.tracy;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.restlet.RestletConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.SpringRouteBuilder;
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
//		    "@timestamp": "2015-04-22T06:34:12.000Z",
//		    "component":"webapp1",
//		    "label": "manual"
//		}
		from("seda:storeTracy")
			// TODO: Store Tracy frames in repository
			// TODO: "_index": "logstash-2015.03.10",
			// TODO: "_type": "prod",
			// TODO: "_id": "p-mi1q2GQrCZcUMbtazDoQ",
			// TODO: "@timestamp": "2015-03-10T23:33:27.707Z",
			.to("elasticsearch://local?operation=INDEX&indexName=tracy&indexType=taskType1")
			.delay(0);
//			.log("${body}");
	}
}
