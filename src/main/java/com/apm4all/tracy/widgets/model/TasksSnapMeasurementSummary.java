package com.apm4all.tracy.widgets.model;

import java.util.ArrayList;

public class TasksSnapMeasurementSummary {
	private ArrayList<TaskMeasurementSummary> tasksSpanMeasurementSummary;
	
	public TasksSnapMeasurementSummary() {
		tasksSpanMeasurementSummary = new ArrayList<TaskMeasurementSummary>() ;
	}
	
	public ArrayList<TaskMeasurementSummary> getTasksSpanMeasurementSummary() {
		return tasksSpanMeasurementSummary;
	}

	public void add(TaskMeasurementSummary task) {
		tasksSpanMeasurementSummary.add(task);
	}
}

