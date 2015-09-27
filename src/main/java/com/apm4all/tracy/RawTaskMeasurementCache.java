package com.apm4all.tracy;

public class RawTaskMeasurementCache {

	/**
	 * Obtains Task measurement cached data for the chosen application tasks 
	 * @param application
	 * @param task
	 * @return 
	 */
	public TaskMeasurement getRawMeasurement(String application, String task) {
		return new StaticTaskMeasurement(application, task);
		// TODO Auto-generated method stub
	}
}
