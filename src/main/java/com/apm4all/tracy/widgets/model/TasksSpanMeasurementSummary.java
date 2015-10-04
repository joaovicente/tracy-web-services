package com.apm4all.tracy.widgets.model;

import java.util.ArrayList;

public class TasksSpanMeasurementSummary {
	private ArrayList<TaskMeasurementSummary> tasksSpanMeasurementSummary;
	
	public TasksSpanMeasurementSummary() {
		tasksSpanMeasurementSummary = new ArrayList<TaskMeasurementSummary>() ;
	}
	
	public ArrayList<TaskMeasurementSummary> getTasksSpanMeasurementSummary() {
		return tasksSpanMeasurementSummary;
	}

	public void add(TaskMeasurementSummary task) {
		tasksSpanMeasurementSummary.add(task);
	}
}
