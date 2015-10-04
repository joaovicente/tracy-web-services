package com.apm4all.tracy.widgets.model;

import java.util.ArrayList;
import java.util.List;

public class SingleApdexTimechart {
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

	public boolean hasNoElements() {
		return timeSequence.isEmpty();
	}

	public void trimLeft(int i) {
		this.timeSequence.remove(0);
		this.apdexScores.remove(0);
	}
}
