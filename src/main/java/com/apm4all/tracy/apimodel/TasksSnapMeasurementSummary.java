package com.apm4all.tracy.apimodel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;

@ApiModel(description = "Task Snap measurement summary")
public class TasksSnapMeasurementSummary {
	private ArrayList<TaskMeasurementSummary> tasksSpanMeasurementSummary;
	
	public TasksSnapMeasurementSummary() {
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

