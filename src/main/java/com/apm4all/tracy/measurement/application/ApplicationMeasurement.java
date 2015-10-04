package com.apm4all.tracy.measurement.application;

import com.apm4all.tracy.widgets.model.LatencyHistogram;
import com.apm4all.tracy.widgets.model.MultiApdexTimechart;
import com.apm4all.tracy.widgets.model.SingleApdexTimechart;
import com.apm4all.tracy.widgets.model.TasksSnapMeasurementSummary;
import com.apm4all.tracy.widgets.model.TasksSpanMeasurementSummary;
import com.apm4all.tracy.widgets.model.VitalsTimechart;

public interface ApplicationMeasurement {
	
	public abstract MultiApdexTimechart getmultiApdexTimechart();

	public abstract TasksSpanMeasurementSummary getTasksSpanMeasurementSummary();

	public abstract TasksSnapMeasurementSummary getTasksSnapMeasurementSummary();

}
