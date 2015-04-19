package com.apm4all.tracy;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.SpringRouteBuilder;

public class RouteBuilder extends SpringRouteBuilder {
	
	@Override
	public void configure() throws Exception {
		from("restlet:http://localhost:8050/tracy/segment?restletMethod=POST")
			.to("seda:tracySegmentProcessor")
			.setBody(simple("{\"tracySegmentId\":\"123456\"}"));
		
		from("seda:tracySegmentProcessor")
			.unmarshal().json(JsonLibrary.Jackson)
			.transform().simple("${body[tracySegment]}")
			// TODO: Validate tracySegment (i.e. filter)
			.split(body())
			.to("seda:storeTracy");
		
		from("seda:storeTracy")
			// TODO: Store Tracy frames in repository
			.to("stream:out");
	}
}
