package com.apm4all.tracy;

import java.util.TreeMap;

public class RawTaskMeasurementSequence {
	private TaskConfig taskConfig;
	private TreeMap<Long, TimeboxedRawTaskMeasurement> measurementSequence;
	
	public RawTaskMeasurementSequence(TaskConfig taskConfig) {
		this.taskConfig = taskConfig;
	}
	
	public void append(Long boxTime, TimeboxedRawTaskMeasurement measurement)	{
		measurementSequence.put(boxTime, measurement);
	}
	
	public String getApplication()	{
		return taskConfig.getApplication();
	}
	
	public String getTask()	{
		return taskConfig.getTask();
	}
	
	public Long getLastestTime() {
		return measurementSequence.lastKey();
	}
}
