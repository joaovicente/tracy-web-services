package com.apm4all.tracy.widgets.model;

import java.util.ArrayList;
import java.util.List;

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

	public ArrayList<Long> getTimeSequence() {
		return timeSequence;
	}

	public void setTimeSequence(List<Long> list) {
		this.timeSequence = new ArrayList<Long>(list);
	}

	public ArrayList<Double> getApdexScores() {
		return apdexScores;
	}

	public void setApdexScores(List<Double> apdexScores) {
		this.apdexScores = new ArrayList<Double>(apdexScores);
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public String getRttUnit() {
		return rttUnit;
	}

	public void setRttUnit(String rttUnit) {
		this.rttUnit = rttUnit;
	}

	public Integer getRttT() {
		return rttT;
	}

	public void setRttT(Integer rttT) {
		this.rttT = rttT;
	}

	public Integer getRttF() {
		return rttF;
	}

	public void setRttF(Integer rttF) {
		this.rttF = rttF;
	}

	public void setTimeSequence(ArrayList<Long> timeSequence) {
		this.timeSequence = timeSequence;
	}

	public void setApdexScores(ArrayList<Double> apdexScores) {
		this.apdexScores = apdexScores;
	}

	public boolean hasNoElements() {
		return timeSequence.isEmpty();
	}

	public void trimLeft(int i) {
		this.timeSequence.remove(0);
		this.apdexScores.remove(0);
	}
}
