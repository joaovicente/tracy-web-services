package com.apm4all.tracy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class TaskMeasurement {
	private LinkedHashMap<String, Object> dapdexTimechart;
	private LinkedHashMap<String, Object> vitalsTimechart;
	private LinkedHashMap<String, Object> latencyHistogram;

	//FIXME: Refactor into StaticTaskMeasurement, NotSoFastTaskMeasurement, BurstyTaskMeasurement and DynamicTaskMeasurement
    public TaskMeasurement()	{
    	staticMeasurement();
    }
    
    public TaskMeasurement(String application, String task)	{
    	notSoFast();
    }
    
    private void notSoFast()	{
    	//TODO: Establish current time
    	//TODO: Establish current time rounded to snap 
    	//TODO: Establish how many elements (span/snap) (N)
    	//TODO: Establish earliest time in span
    	//TODO: Find how many timeSequence entries to drop (M)
    	//TODO: Drop M items from each ArrayList
    	//TODO: Append new items to dapdexTimechart and vitalsTimechart until N is met (appendTimecharts())
    	//TODO: Update latencyHistogram with updateLatencyHistogram() 
    		//- count must match vitalsCount, heavier bins around p95 
    }
    
    private void staticMeasurement()	{
    	// APDEX timechart
    	dapdexTimechart = new LinkedHashMap<String, Object>();
    	ArrayList<Long> timeSequence = new ArrayList<Long>(Arrays.asList(new Long[]
    			{1439312400000L,1439312401900L,1439312403800L,1439312405700L,
    			1439312407600L,1439312409500L,1439312411400L,1439312413300L,
    			1439312415200L,1439312417100L,1439312419000L,1439312420900L,
    			1439312422800L,1439312424700L,1439312426600L,1439312428500L}
    	));
    	ArrayList<Double> dapdexScores = new ArrayList<Double>(Arrays.asList(new Double[]
    			{0.99,0.98,0.99,0.93,0.94,0.97,0.95,0.97,0.83,0.93,0.97,0.94,0.96,0.98,0.95,0.96}
    	));
    	dapdexTimechart.put("timeSequence", timeSequence);
    	dapdexTimechart.put("dapdexScores", dapdexScores);
    	
    	// Vitals Timechart
    	vitalsTimechart = new LinkedHashMap<String, Object>();
    	ArrayList<Integer> count = new ArrayList<Integer>(Arrays.asList(new Integer[]
    			 {200,243,254,234,253,265,245,247,765,243,265,273,247,256,236,245}
    	));
    	ArrayList<Integer> errors = new ArrayList<Integer>(Arrays.asList(new Integer[]
    			{1,2,1,2,1,2,1,2,4,1,2,1,2,1,2,1}
    	));
    	ArrayList<Integer> p95 = new ArrayList<Integer>(Arrays.asList(new Integer[]
    			{110,132,141,143,151,134,123,131,111,125,123,143,122,156,116,145}
    	));
    	vitalsTimechart.put("timeSequence", timeSequence);
    	vitalsTimechart.put("count", count);
    	vitalsTimechart.put("errors", errors);
    	vitalsTimechart.put("p95", p95);
    	
    	// Latency histogram
    	latencyHistogram = new LinkedHashMap<String, Object>();
    	ArrayList<String> bins = new ArrayList<String>(Arrays.asList(new String[]
    			{"1200","1140-1200","1080-1139","1020-1079","960-1019","900-959","840-899","780-839","720-779","660-719"
    			,"600-659","540-599","480-539","420-479","360-419","300-359","240-299","180-239","120-179","60-119","0-59"}
    	));
    	ArrayList<String> rttZone = new ArrayList<String>(Arrays.asList(new String[]
    			{"Frustrated","Tolerating","Tolerating","Tolerating","Tolerating",
    			"Tolerating","Tolerating","Tolerating","Tolerating","Tolerating"
    			,"Tolerating","Tolerating","Tolerating","Tolerating","Tolerating"
    			,"Tolerating","Satisfied","Satisfied","Satisfied","Satisfied","Satisfied"}
    	));
    	ArrayList<Integer> lhCount = new ArrayList<Integer>(Arrays.asList(new Integer[]
    			{4,8,22,22,22,22,76,89,44,134,134,134,178,178,268,447,670,1548,312,89,44}
    	));
    	latencyHistogram.put("timeSequence", timeSequence);
    	latencyHistogram.put("bins", bins);
    	latencyHistogram.put("rttZone", rttZone);
    	latencyHistogram.put("count", lhCount);
    	
    }
    
	public LinkedHashMap<String, Object> getDapdexTimechart() {
		return dapdexTimechart;
	}
	public LinkedHashMap<String, Object> getVitalsTimechart() {
		return vitalsTimechart;
	}
	public LinkedHashMap<String, Object> getLatencyHistogram() {
		return latencyHistogram;
	}
}
