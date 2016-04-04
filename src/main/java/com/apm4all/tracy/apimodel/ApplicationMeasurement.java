package com.apm4all.tracy.apimodel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Provides the Application measurement")
public interface ApplicationMeasurement {
	@ApiModelProperty(value = "Timechart containing APDEX scores for all Application Tasks", required = true)
	MultiApdexTimechart getMultiApdexTimechart();
	
	@ApiModelProperty(value = "Span (measurement window) summary metrics (e.g. last 4 hours)", required = true)
	TasksSpanMeasurementSummary getTasksSpanMeasurementSummary();

	@ApiModelProperty(value = "Span (last tick) summary metrics (e.g. last 15 minutes)", required = true)
	TasksSnapMeasurementSummary getTasksSnapMeasurementSummary();
}
