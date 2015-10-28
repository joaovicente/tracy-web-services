package com.apm4all.tracy.simulations;

import static org.junit.Assert.*;

import org.junit.Test;

public class StaticTaskMeasurementTest {

	@Test
	public void test() {
		StaticTaskMeasurement taskMeasurement = 
				new StaticTaskMeasurement("SimulatedApp", "StaticGoodTask");
		int i = taskMeasurement.getSingleApdexTimechart().getRttT();
		assertEquals(180, i);
	}
}
