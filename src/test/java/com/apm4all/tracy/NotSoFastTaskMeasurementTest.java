package com.apm4all.tracy;

import static org.junit.Assert.*;

import org.junit.Test;

import com.apm4all.tracy.apimodel.TaskMeasurement;
import com.apm4all.tracy.simulations.NotSoFastTaskMeasurement;

public class NotSoFastTaskMeasurementTest {

	@Test
	public void testLatencyHistogram() {
    	TaskMeasurement taskMeasurement = new NotSoFastTaskMeasurement("", "");
    	System.out.println("LatencyHistogram bins: " + taskMeasurement.getLatencyHistogram().getBins());
    	System.out.println("LatencyHistogram rttZones: " + taskMeasurement.getLatencyHistogram().getRttZone());
    	System.out.println("LatencyHistogram count: " + taskMeasurement.getLatencyHistogram().getCount());
    	assertEquals(17, taskMeasurement.getLatencyHistogram().getBins().size());
	}
}