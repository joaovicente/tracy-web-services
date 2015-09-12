package com.apm4all.tracy;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import com.apm4all.tracy.widgets.LatencyHistogram;
import com.apm4all.tracy.widgets.SingleApdexTimechart;
import com.apm4all.tracy.widgets.VitalsTimechart;

public class NotSoFastTaskMeasurement implements TaskMeasurement {
	static private SingleApdexTimechart singleApdexTimechart = new SingleApdexTimechart();
	static private VitalsTimechart vitalsTimechart = new VitalsTimechart();
	static private LatencyHistogram latencyHistogram = new LatencyHistogram();

    public NotSoFastTaskMeasurement(String application, String task)	{
    	notSoFast();
    }
    
    private void notSoFast()	{
    	//long snap = 60000 * 15; // in msec (15 minutes) 
    	//long span = 60000 * 60 * 4; // in msec (4 hours) 
    	long snap = 1000; // 1 second
    	long span = 16000; // 30 seconds
    	
    	// Establish current time
    	long now = System.currentTimeMillis();
    	// Establish current time rounded to snap
    	long endTime = now - now%snap;
    	// Establish earliest time in span
    	long startTime = endTime - span;
    	// Remove older metrics
    	removeOlderMetrics(startTime);
    	// Append new items to dapdexTimechart and vitalsTimechart until N is met (appendTimecharts())
    	appendTimechartMetrics(startTime, endTime, snap);
    	appendLatencyHistogram(startTime, endTime, snap);
    	// Update latencyHistogram with updateLatencyHistogram() 
    }
    
	private int removeOlderMetrics(long startTime) {
//		ArrayList<Long> timeSequence = singleApdexTimechart.getTimeSequence();
		int numItemsToRemove = 0;
//		System.out.println("singleApdexTimechart (pre-removal) size = " +  singleApdexTimechart.getTimeSequence().size());
		for (int i = 0; i < singleApdexTimechart.getTimeSequence().size(); i++) {
			if (singleApdexTimechart.getTimeSequence().get(i) < startTime)	{
//				System.out.println("singleApdexTimechart sch4removal " + singleApdexTimechart.getTimeSequence().get(i));
				numItemsToRemove++;
			}
			else	{
//				System.out.println("singleApdexTimechart NOT sch4removal " + singleApdexTimechart.getTimeSequence().get(i));
			}
		}
//		System.out.println("singleApdexTimechart removing " +  numItemsToRemove);
		while(numItemsToRemove > 0)	{
//			System.out.println("singleApdexTimechart removing " + singleApdexTimechart.getTimeSequence().get(0));
			singleApdexTimechart.trimLeft(1);
            vitalsTimechart.trimLeft(1);
			numItemsToRemove--;
		}
		return 0;
	}
	
	public static int randInt(int min, int max) {
	    Random rand = new Random();
	    return rand.nextInt((max - min) + 1) + min;
	}
	
	public static double randApdex(double min, double max) {
	    Random rand = new Random();
	    double randomValue = min + (max - min) * rand.nextDouble();
	    Math.round(randomValue);
	    DecimalFormat newFormat = new DecimalFormat("#.##");
	    double randomValueRounded =  Double.valueOf(newFormat.format(randomValue));
	    return randomValueRounded;
	}
	
	private void appendTimechartMetrics(long startTime, long endTime, long snap) {
		ArrayList<Long> timeSequence = singleApdexTimechart.getTimeSequence();
		
		System.out.println("singleApdexTimechart startTime=" + startTime + ", endTime=" + endTime + ", snap=" + snap);
		// Add APDEX scores for missing buckets within range
		for (long i = startTime ; i < endTime ; i+=snap)	{
			// Add only timeSequence data not yet populated 
			if ( (timeSequence.size() > 0 // timeSequence has data AND i is higher than last timeSequence
					&& i > timeSequence.get(timeSequence.size()-1)) 
					|| timeSequence.size() == 0)	{ // OR if timeSequence empty
				
				
				System.out.println("singleApdexTimechart adding i=" + i);
				// APDEX score
				timeSequence.add(i);
				// apdexScore to be random value between POOR and FAIR
				double apdexScore = randApdex(0.60, 0.80);
				singleApdexTimechart.getApdexScores().add(apdexScore);
				
				// Vitals
				vitalsTimechart.getTimeSequence().add(i);
				vitalsTimechart.getCount().add(randInt(160,200));
				vitalsTimechart.getErrors().add(randInt(0,3));
				vitalsTimechart.getP95().add((int) ((1-apdexScore)*1000));
			}
			
		}
		System.out.println("singleApdexTimechart size =" +  timeSequence.size());
		
	}
	
    private void appendLatencyHistogram(long startTime, long endTime, long snap) {
		// TODO Add Vitals scores for missing buckets within range
		// TODO Add latency histogram bins and counts
    	// TODO count must match vitalsCount, heavier bins around p95 
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
