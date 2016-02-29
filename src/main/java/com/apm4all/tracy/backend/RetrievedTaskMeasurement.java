package com.apm4all.tracy.backend;

import com.apm4all.tracy.apimodel.LatencyHistogram;
import com.apm4all.tracy.apimodel.SingleApdexTimechart;
import com.apm4all.tracy.apimodel.VitalsTimechart;

public class RetrievedTaskMeasurement {
	private SingleApdexTimechart singleApdexTimechart;
	private VitalsTimechart vitalsTimechart;
	private LatencyHistogram latencyHistogram;
	private String application;
	private String task;
	

}
