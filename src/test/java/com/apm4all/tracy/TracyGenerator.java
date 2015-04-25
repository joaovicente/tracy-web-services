package com.apm4all.tracy;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TracyGenerator {
	final String HOSTNAME = "localhost";
	final int PORT = 8050;

	@Before
	public void setUp()	{
		new TracyPublisherFactory.Builder(TracyPublisherFactory.Type.HTTP_CLIENT)
		.hostname(HOSTNAME)
		.port(PORT)
		// waitForResponse allows testing publish() received a 2xx http status
//		.waitForResponse(true) 
//		.debug(true)
		.build();		
	}
	
	@Test
	public void test() throws InterruptedException {
		final String labelA = "main", labelB = "foo", labelC = "bar";
		String tracySegment;
		int i = 0;
		while (i<1000) {
			Tracy.setContext("id-" + Integer.toString(i), "null", "MyWebApp");
			Tracy.before(labelA);
			Tracy.before(labelB);
			Thread.sleep(100);
			Tracy.after(labelB);
			Tracy.before(labelC);
			Thread.sleep(50);
			Tracy.after(labelC);
			Tracy.after(labelA);
			tracySegment = Tracy.getEventsAsJsonTracySegment();
//			for (String event : Tracy.getEventsAsJson())	{
//				System.out.println(event);
//			}
			assertTrue(TracyPublisherFactory.getInstance().publish(tracySegment));
			i++;
			Tracy.clearContext();
			if (i % 10 == 0)	{
				System.out.println("Publishing Tracy segment " + i);
			}
		Thread.sleep(100);
		}
	}

}
