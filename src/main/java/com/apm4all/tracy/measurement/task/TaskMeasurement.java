package com.apm4all.tracy.measurement.task;

import com.apm4all.tracy.widgets.model.LatencyHistogram;
import com.apm4all.tracy.widgets.model.SingleApdexTimechart;
import com.apm4all.tracy.widgets.model.VitalsTimechart;

public interface TaskMeasurement {

	//TODO: Implement Refactor into StaticTaskMeasurement, BurstyTaskMeasurement and DynamicTaskMeasurement
	public abstract SingleApdexTimechart getSingleApdexTimechart();

	public abstract VitalsTimechart getVitalsTimechart();

	public abstract LatencyHistogram getLatencyHistogram();

}