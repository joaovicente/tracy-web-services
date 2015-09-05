package com.apm4all.tracy;

public class TaskMeasurementService {

    /**
     * Gets a measurement for a given application, task
     *
     * @param application the application name
     * @param task the task name
     * @return the measurement, or <tt>null</tt> if the application or task are invalid 
     */
    public TaskMeasurement getTaskMeasurement(String application, String task) {
    	TaskMeasurement taskMeasurement = null;
    	if (task.equals("Not-so-fast"))	{
    		taskMeasurement = new NotSoFastTaskMeasurement(application, task);
    	}
    	else if (task.equals("Static"))	{
    		taskMeasurement = new StaticTaskMeasurement(application, task);
    	}
    	return taskMeasurement;
    }
	
}
