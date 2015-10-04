package com.apm4all.tracy.simulations;

import java.util.Arrays;

import com.apm4all.tracy.measurement.task.TaskMeasurement;
import com.apm4all.tracy.widgets.model.LatencyHistogram;
import com.apm4all.tracy.widgets.model.SingleApdexTimechart;
import com.apm4all.tracy.widgets.model.VitalsTimechart;

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
    	singleApdexTimechart.setApplication("Static");
    	singleApdexTimechart.setTask("sa");
    	singleApdexTimechart.setRttUnit("ms");
    	singleApdexTimechart.setRttT(180);
    	singleApdexTimechart.setRttF(720);
    	
    	// APDEX timechart
    	singleApdexTimechart.setTimeSequence(Arrays.asList(new Long[]
    			{1443985200000L, 1443986100000L, 1443987000000L, 1443987900000L, 
    			1443988800000L, 1443989700000L, 1443990600000L, 1443991500000L, 
    			1443992400000L, 1443993300000L, 1443994200000L, 1443995100000L, 
    			1443996000000L, 1443996900000L, 1443997800000L, 1443998700000L}

    			));
    	singleApdexTimechart.setApdexScores(Arrays.asList(new Double[]
    			{0.99,0.98,0.99,0.93,0.94,0.97,0.95,0.97,0.83,0.93,0.97,0.94,0.96,0.98,0.95,0.96}
    			));
    	
    	// Vitals Timechart
    	vitalsTimechart.setTimeSequence(Arrays.asList(new Long[]
    			{1443985200000L, 1443986100000L, 1443987000000L, 1443987900000L, 
    			1443988800000L, 1443989700000L, 1443990600000L, 1443991500000L, 
    			1443992400000L, 1443993300000L, 1443994200000L, 1443995100000L, 
    			1443996000000L, 1443996900000L, 1443997800000L, 1443998700000L}
    			));
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
