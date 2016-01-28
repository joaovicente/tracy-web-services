package com.apm4all.tracy.apimodel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;

@ApiModel(description = "A timechart containing Multiple APDEX scores")
public class MultiApdexTimechart {
	private ArrayList<SingleApdexTimechart> apdexTimechart;
	public MultiApdexTimechart() {
		apdexTimechart = new ArrayList<SingleApdexTimechart>();
	}
	
	@ApiModelProperty(value = "List of Task APDEX scores", required = true)
	public ArrayList<SingleApdexTimechart> getTasks() {
		return apdexTimechart;
	}
	
	public void add(SingleApdexTimechart seriesToAdd) {
		apdexTimechart.add(seriesToAdd);
	}
}
