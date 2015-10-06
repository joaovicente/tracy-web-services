package com.apm4all.tracy.measurement.application;

import com.apm4all.tracy.widgets.model.MultiApdexTimechart;
import com.apm4all.tracy.widgets.model.TasksSnapMeasurementSummary;
import com.apm4all.tracy.widgets.model.TasksSpanMeasurementSummary;

public interface ApplicationMeasurement {
	
	public abstract MultiApdexTimechart getMultiApdexTimechart();

	public abstract TasksSpanMeasurementSummary getTasksSpanMeasurementSummary();

	public abstract TasksSnapMeasurementSummary getTasksSnapMeasurementSummary();

}
