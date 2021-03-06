package com.apm4all.tracy.services;

import com.apm4all.tracy.apimodel.TaskMeasurement;
import com.apm4all.tracy.simulations.StaticBatchTaskMeasurement;
import com.apm4all.tracy.simulations.StaticTaskMeasurement;


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
    	if (application.equals("demo-static-app"))	{
    		taskMeasurement = new StaticTaskMeasurement(application, task);
    	}
    	else if (application.equals("demo-static-batch-app"))	{
    		taskMeasurement = new StaticBatchTaskMeasurement(application, task);
    	}
    	return taskMeasurement;
    }
	
}
