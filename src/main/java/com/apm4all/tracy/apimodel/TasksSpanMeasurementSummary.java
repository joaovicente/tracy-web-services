package com.apm4all.tracy.apimodel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;

@ApiModel(description = "Task Span measurement summary")
public class TasksSpanMeasurementSummary {
	private ArrayList<TaskMeasurementSummary> tasksSpanMeasurementSummary;
	
	public TasksSpanMeasurementSummary() {
		tasksSpanMeasurementSummary = new ArrayList<TaskMeasurementSummary>() ;
	}
	
	@ApiModelProperty(value = "Task measurement summaries", required = true)
	public ArrayList<TaskMeasurementSummary> getTasks() {
		return tasksSpanMeasurementSummary;
	}

	public void add(TaskMeasurementSummary task) {
		tasksSpanMeasurementSummary.add(task);
	}
}
