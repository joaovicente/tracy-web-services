package com.apm4all.tracy.apimodel;

import java.util.ArrayList;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "A timechart containing APDEX scores")
public class SingleApdexTimechart {
	private String application;
	private String task;
    private String rttUnit; // ms, s, h, d
    private Integer rttT;
    private Integer rttF;
	private ArrayList<Long> timeSequence;
	private ArrayList<Double> apdexScores;
	
	public SingleApdexTimechart()	{
		timeSequence = new ArrayList<Long>();
		apdexScores = new ArrayList<Double>();
	}

	@ApiModelProperty(value = "Time sequence in epoch msec", required = true)
	public ArrayList<Long> getTimeSequence() {
		return timeSequence;
	}

	public void setTimeSequence(List<Long> list) {
		this.timeSequence = new ArrayList<Long>(list);
	}

	@ApiModelProperty(value = "The APDEX scores", required = true)
	public ArrayList<Double> getApdexScores() {
		return apdexScores;
	}

	public void setApdexScores(List<Double> apdexScores) {
		this.apdexScores = new ArrayList<Double>(apdexScores);
	}

	@ApiModelProperty(value = "The Application", required = true)
	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	@ApiModelProperty(value = "The Task", required = true)
	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	@ApiModelProperty(value = "The Response Time Threshold unit", required = true, allowableValues = "ms,s,m,h")
	public String getRttUnit() {
		return rttUnit;
	}

	public void setRttUnit(String rttUnit) {
		this.rttUnit = rttUnit;
	}

	@ApiModelProperty(value = "The Response Time Threshold - Tolerating", required = true)
	public Integer getRttT() {
		return rttT;
	}

	public void setRttT(Integer rttT) {
		this.rttT = rttT;
	}

	@ApiModelProperty(value = "The Response time threshold - Frustrated", required = true)
	public Integer getRttF() {
		return rttF;
	}

	public void setRttF(Integer rttF) {
		this.rttF = rttF;
	}

	public boolean hasNoElements() {
		return timeSequence.isEmpty();
	}

	public void trimLeft(int i) {
		this.timeSequence.remove(0);
		this.apdexScores.remove(0);
	}
}
