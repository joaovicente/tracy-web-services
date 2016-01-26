package com.apm4all.tracy.apimodel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Provides the Task measurement")
public interface TaskMeasurement {

	//TODO: Implement Refactor into StaticTaskMeasurement, BurstyTaskMeasurement and DynamicTaskMeasurement
	@ApiModelProperty(value = "Single timechart", required = true)
	public abstract SingleApdexTimechart getSingleApdexTimechart();

	@ApiModelProperty(value = "Vitals timechart", required = true)
	public abstract VitalsTimechart getVitalsTimechart();

	@ApiModelProperty(value = "Latency histogram", required = true)
	public abstract LatencyHistogram getLatencyHistogram();

}