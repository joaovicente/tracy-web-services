package com.apm4all.tracy.widgets.model;

import java.util.ArrayList;

public class MultiApdexTimechart {
	private ArrayList<SingleApdexTimechart> apdexTimechart;
	public MultiApdexTimechart() {
		apdexTimechart = new ArrayList<SingleApdexTimechart>();
	}
	
	public ArrayList<SingleApdexTimechart> getTasks() {
		return apdexTimechart;
	}
	
	public void add(SingleApdexTimechart seriesToAdd) {
		apdexTimechart.add(seriesToAdd);
	}
}
