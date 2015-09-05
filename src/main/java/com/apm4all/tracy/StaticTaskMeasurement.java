package com.apm4all.tracy;

import java.util.Arrays;
import com.apm4all.tracy.widgets.LatencyHistogram;
import com.apm4all.tracy.widgets.SingleApdexTimechart;
import com.apm4all.tracy.widgets.VitalsTimechart;

public class StaticTaskMeasurement implements TaskMeasurement {
	static private SingleApdexTimechart singleApdexTimechart = new SingleApdexTimechart();
	static private VitalsTimechart vitalsTimechart = new VitalsTimechart();
	static private LatencyHistogram latencyHistogram = new LatencyHistogram();

    public StaticTaskMeasurement(String application, String task)	{
    	if (singleApdexTimechart.hasNoElements())	{
    		// Initialize only once
    		staticMeasurement();
    	}
    }
    
    private void staticMeasurement()	{
    	// APDEX timechart
    	singleApdexTimechart.setTimeSequence(Arrays.asList(new Long[]
    			{1439312400000L,1439312401900L,1439312403800L,1439312405700L,
    			1439312407600L,1439312409500L,1439312411400L,1439312413300L,
    			1439312415200L,1439312417100L,1439312419000L,1439312420900L,
    			1439312422800L,1439312424700L,1439312426600L,1439312428500L}
    			));
    	singleApdexTimechart.setApdexScores(Arrays.asList(new Double[]
    			{0.99,0.98,0.99,0.93,0.94,0.97,0.95,0.97,0.83,0.93,0.97,0.94,0.96,0.98,0.95,0.96}
    			));
    	
    	// Vitals Timechart
    	vitalsTimechart.setCount(Arrays.asList(new Integer[]
    			 {200,243,254,234,253,265,245,247,765,243,265,273,247,256,236,245}
    	));
    	
    	vitalsTimechart.setErrors(Arrays.asList(new Integer[]
    			{1,2,1,2,1,2,1,2,4,1,2,1,2,1,2,1}
    	));
    	vitalsTimechart.setP95(Arrays.asList(new Integer[]
    			{110,132,141,143,151,134,123,131,111,125,123,143,122,156,116,145}
    	));
    	
    	// Latency histogram
    	latencyHistogram.setBins(Arrays.asList(new String[]
    			{"1200","1140-1200","1080-1139","1020-1079","960-1019","900-959","840-899","780-839","720-779","660-719"
    			,"600-659","540-599","480-539","420-479","360-419","300-359","240-299","180-239","120-179","60-119","0-59"}
    	));
    	latencyHistogram.setRttZone(Arrays.asList(new String[]
    			{"Frustrated","Tolerating","Tolerating","Tolerating","Tolerating",
    			"Tolerating","Tolerating","Tolerating","Tolerating","Tolerating"
    			,"Tolerating","Tolerating","Tolerating","Tolerating","Tolerating"
    			,"Tolerating","Satisfied","Satisfied","Satisfied","Satisfied","Satisfied"}
    	));
    	latencyHistogram.setCount(Arrays.asList(new Integer[]
    			{4,8,22,22,22,22,76,89,44,134,134,134,178,178,268,447,670,1548,312,89,44}
    	));
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
