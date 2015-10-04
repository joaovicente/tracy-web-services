package com.apm4all.tracy.widgets.model;

import java.util.ArrayList;
import java.util.List;

public class LatencyHistogram {
	private ArrayList<String> bins;
	private ArrayList<String> rttZone;
	private ArrayList<Integer> count;
	
	public LatencyHistogram()	{
		bins = new ArrayList<String>();
		rttZone = new ArrayList<String>();
		count = new ArrayList<Integer>();
		
	}
	
	public ArrayList<String> getBins() {
		return bins;
	}
	public void setBins(List<String> bins) {
		this.bins = new ArrayList<String>(bins);
	}
	public ArrayList<String> getRttZone() {
		return rttZone;
	}
	public void setRttZone(List<String> rttZone) {
		this.rttZone = new ArrayList<String>(rttZone);
	}
	public ArrayList<Integer> getCount() {
		return count;
	}
	public void setCount(List<Integer> count) {
		this.count = new ArrayList<Integer>(count);
	}
}
