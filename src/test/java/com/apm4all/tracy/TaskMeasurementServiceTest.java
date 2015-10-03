package com.apm4all.tracy;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TaskMeasurementServiceTest {

	private TaskMeasurementService svc;
	private	RawTaskMeasurementCache cache;

	@Before
	public void setUp() throws Exception {
		svc = new TaskMeasurementService();
		cache = new RawTaskMeasurementCache();
		svc.setMeasurementCache(cache);
	}

	@Test
	public void getTaskMeasurementTest() {
		TaskMeasurement measurement = svc.getTaskMeasurement("Simulated", "Static");
		assertNotNull(measurement);
	}
	
	@Test
	public void refreshCache()	{
		svc.refreshCache();
		cache.getSequence("SimulatedBad", "NotSoFast");
	}
}
