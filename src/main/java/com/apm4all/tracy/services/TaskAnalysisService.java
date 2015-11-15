package com.apm4all.tracy.services;

import com.apm4all.tracy.analysis.task.TaskAnalysis;

public class TaskAnalysisService {

    /**
     * Gets a measurement for a given application, task
     *
     * @param application the application name
     * @param task the task name
     * @return the measurement, or <tt>null</tt> if the application or task are invalid 
     */
    public TaskAnalysis getTaskAnalysis(String application, String task, 
    		String earliest, String latest, String filter, String sort, 
    		String limit, String offset) {
    	TaskAnalysis taskAnalysis = new TaskAnalysis(
    			application, task, Long.parseLong(earliest), Long.parseLong(latest), 
    			filter, sort, Integer.parseInt(limit), Integer.parseInt(offset));
    	
    	return taskAnalysis;
    }
}