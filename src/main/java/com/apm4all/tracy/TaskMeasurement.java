package com.apm4all.tracy;

import com.apm4all.tracy.widgets.LatencyHistogram;
import com.apm4all.tracy.widgets.SingleApdexTimechart;
import com.apm4all.tracy.widgets.VitalsTimechart;

public interface TaskMeasurement {

	//TODO: Implement Refactor into StaticTaskMeasurement, BurstyTaskMeasurement and DynamicTaskMeasurement
	public abstract SingleApdexTimechart getSingleApdexTimechart();

	public abstract VitalsTimechart getVitalsTimechart();

	public abstract LatencyHistogram getLatencyHistogram();

}