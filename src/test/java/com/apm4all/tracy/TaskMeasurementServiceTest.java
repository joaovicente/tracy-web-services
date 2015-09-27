package com.apm4all.tracy;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TaskMeasurementServiceTest {

	private TaskMeasurementService svc;

	@Before
	public void setUp() throws Exception {
		RawTaskMeasurementCollector collector = new RawTaskMeasurementCollector();
		RawTaskMeasurementCache cache = new RawTaskMeasurementCache();
//		TaskConfigDao taskConfig = new TaskConfigDao();
		svc = new TaskMeasurementService();
		svc.setMeasurementCollector(collector);
		svc.setMeasurementCache(cache);
	}

	@Test
	public void getTaskMeasurementTest() {
		TaskMeasurement measurement = svc.getTaskMeasurement("Simulated", "Static");
		assertNotNull(measurement);
	}
}
