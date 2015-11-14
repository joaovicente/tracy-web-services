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
    public TaskAnalysis getTaskAnalysis(String application, String task) {
    	TaskAnalysis taskAnalysis = new TaskAnalysis(application, task);
    	return taskAnalysis;
    }
}