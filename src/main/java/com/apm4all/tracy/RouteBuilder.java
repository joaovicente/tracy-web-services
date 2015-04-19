package com.apm4all.tracy;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.SpringRouteBuilder;

public class RouteBuilder extends SpringRouteBuilder {
	
	@Override
	public void configure() throws Exception {
		from("restlet:http://localhost:8050/tracy/segment?restletMethod=POST")
			// Tracy publishing should never block the sender
			// waitForTaskToComplete allows endpoint to respond
			// without having to wait until tracy is finally stored
			.to("seda:tracySegmentProcessor?waitForTaskToComplete=Never")
			//TODO: Return taskId-component as reference with HTTP 202 code
			.setBody(simple("{\"tracySegmentId\":\"123456\"}"));
		
		from("seda:tracySegmentProcessor")
			.unmarshal().json(JsonLibrary.Jackson)
			.transform().simple("${body[tracySegment]}")
			// TODO: Validate tracySegment messages
			// TODO: Send invalid segments to audit log
			.split(body())
			.to("seda:storeTracy");
		
		from("seda:storeTracy")
			// TODO: Store Tracy frames in repository
			.delay(0);
//			.log("${body}");
	}
}
