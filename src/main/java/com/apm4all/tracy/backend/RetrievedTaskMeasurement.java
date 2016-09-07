package com.apm4all.tracy.backend;

import com.apm4all.tracy.apimodel.LatencyHistogram;
import com.apm4all.tracy.apimodel.SingleApdexTimechart;
import com.apm4all.tracy.apimodel.TaskMeasurement;
import com.apm4all.tracy.apimodel.VitalsTimechart;

public class RetrievedTaskMeasurement implements TaskMeasurement {
	private String application;
	private String task;
	private SingleApdexTimechart singleApdexTimechart;
	private VitalsTimechart vitalsTimechart;
	private LatencyHistogram latencyHistogram;
	
	public RetrievedTaskMeasurement(String application, String task) {
		super();
		this.application = application;
		this.task = task;
		this.singleApdexTimechart = new SingleApdexTimechart();
		this.vitalsTimechart = new VitalsTimechart();
		this.latencyHistogram = new LatencyHistogram();
	}

	@Override
	public SingleApdexTimechart getSingleApdexTimechart() {
		return singleApdexTimechart;
	}

	@Override
	public VitalsTimechart getVitalsTimechart() {
		return vitalsTimechart;
	}

	@Override
	public LatencyHistogram getLatencyHistogram() {
		return latencyHistogram;
	}

}
