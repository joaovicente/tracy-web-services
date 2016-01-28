package com.apm4all.tracy.apimodel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "A Latency histogram")
public class LatencyHistogram {
	private ArrayList<String> bins;
	private ArrayList<String> rttZone;
	private ArrayList<Integer> count;
	
	public LatencyHistogram()	{
		bins = new ArrayList<String>();
		rttZone = new ArrayList<String>();
		count = new ArrayList<Integer>();
	}
	
	@ApiModelProperty(value = "Time response time range bins, e.g. 0-100, 100-200, >200", required = true)
	public ArrayList<String> getBins() {
		return bins;
	}
	public void setBins(List<String> bins) {
		this.bins = new ArrayList<String>(bins);
	}
	
	@ApiModelProperty(value = "Response Time Threshold Zone. {Satisfied,Tolerating,Frustrated}", required = true)
	public ArrayList<String> getRttZone() {
		return rttZone;
	}
	public void setRttZone(List<String> rttZone) {
		this.rttZone = new ArrayList<String>(rttZone);
	}
	
	@ApiModelProperty(value = "Sample count for each bin", required = true)
	public ArrayList<Integer> getCount() {
		return count;
	}
	public void setCount(List<Integer> count) {
		this.count = new ArrayList<Integer>(count);
	}
}
