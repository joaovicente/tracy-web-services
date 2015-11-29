package com.apm4all.tracy;

import static org.junit.Assert.*;

import org.junit.Test;

import com.apm4all.tracy.simulations.StaticTaskMeasurement;

public class StaticTaskMeasurementTest {

	@Test
	public void test() {
		StaticTaskMeasurement taskMeasurement = 
				new StaticTaskMeasurement("SimulatedApp", "StaticGoodTask");
		int i = taskMeasurement.getSingleApdexTimechart().getRttT();
		assertEquals(180, i);
	}
}
