package com.apm4all.tracy.widgets.model;

import java.util.ArrayList;
import java.util.List;

public class VitalsTimechart {
	private ArrayList<Long> timeSequence;
	private ArrayList<Integer> count;
	private ArrayList<Integer> errors;
	private ArrayList<Double> p95;
	
	public VitalsTimechart()	{
		timeSequence = new ArrayList<Long>();
		count = new ArrayList<Integer>();
		errors = new ArrayList<Integer>();
		p95 = new ArrayList<Double>();
	}

	public ArrayList<Long> getTimeSequence() {
		return timeSequence;
	}

	public void setTimeSequence(List<Long> timeSequence) {
		this.timeSequence = new ArrayList<Long>(timeSequence);
	}

	public ArrayList<Integer> getCount() {
		return count;
	}

	public void setCount(List<Integer> count) {
		this.count = new ArrayList<Integer>(count);
	}

	public ArrayList<Integer> getErrors() {
		return errors;
	}

	public void setErrors(List<Integer> errors) {
		this.errors = new ArrayList<Integer>(errors);
	}

	public ArrayList<Double> getP95() {
		return p95;
	}

	public void setP95(ArrayList<Double> p95s) {
		this.p95 = new ArrayList<Double>(p95s);
	}

	public void trimLeft(int i) {
		this.timeSequence.remove(0);
		this.count.remove(0);
		this.errors.remove(0);
		this.p95.remove(0);
	}

}
