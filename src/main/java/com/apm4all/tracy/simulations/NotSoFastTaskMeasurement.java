package com.apm4all.tracy.simulations;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import com.apm4all.tracy.measurement.task.TaskMeasurement;
import com.apm4all.tracy.widgets.model.LatencyHistogram;
import com.apm4all.tracy.widgets.model.SingleApdexTimechart;
import com.apm4all.tracy.widgets.model.VitalsTimechart;

public class NotSoFastTaskMeasurement implements TaskMeasurement {
	static private SingleApdexTimechart singleApdexTimechart = new SingleApdexTimechart();
	static private VitalsTimechart vitalsTimechart = new VitalsTimechart();
	static private LatencyHistogram latencyHistogram = new LatencyHistogram();
	static private final int ONE_SECOND = 1000;
	static private final int SIXTEEN_SECONDS = 16000;
//	static private final int THIRTY_SECONDS = 30000;
//	static private final int FIFTEEN_MINUTES = 60000 * 15;
//	static private final int FOUR_HOURS = 60000 * 60 * 4;
	static private final double apdexUpperLimit = 0.8; 
	static private final double apdexLowerLimit = 0.7; 
	static private final int countLowerLimit = 160;
	static private final int countUpperLimit = 200;
	static private final int errorLowerLimit = 0;
	static private final int errorUpperLimit = 3;
	static private final int rttT = 300;
	static private final int rttF = 1200;
	private int latencyHistogramFactor = 4;
//	private final int FRUSTRATED_BIN = 0;
	private final int TOLERATING_UPPER_BIN = 1;
	private final int TOLERATING_LOWER_BIN = 12;
	private final int SATISFIED_UPPER_BIN = 13;
	private final int SATISFIED_LOWER_BIN = 16;
	
	// Candidate builder methods
	//.withModel (MeaurementsContainer) // MUST (Stores runtime task config and task metrics)
	//.withApplication ("Demo 1") // MUST
	//.withTask ("notSoFast") // MUST
	//.withRttT (300)
	//.withRttF (withRttT*4)
	//.withSpanInSeconds (16) <- Fast refresh
	//.withSpanInHours (4) <- Low volume
	//.withSnapInSeconds (1) <- Fast refresh
	//.withSnapInMinutes (15) <- Low volume
	//.withLatencyHistBinDivisor (4) means 17 bins
	//.build
	
	// Interface methods
	// update() // Fetch new metrics for this task
	// TODO Think about a MeasurementsContainerUpdater Class responsible for periodically updating tasks to decouple user requests from ES querying

	// *******************************
	// * Base class methods
	// *******************************
	
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

	public static double randDouble(double min, double max) {
	    Random rand = new Random();
	    double randomValue = min + (max - min) * rand.nextDouble();
	    Math.round(randomValue);
	    DecimalFormat newFormat = new DecimalFormat("#.##");
	    double randomValueRounded =  Double.valueOf(newFormat.format(randomValue));
	    return randomValueRounded;
	}

	private int getNumberOfSamples(VitalsTimechart vitals)	{
		int totalSamples = 0;
		for (int i = 0; i < vitals.getCount().size(); i++) {
			totalSamples = totalSamples + vitals.getCount().get(i);
		}
		return totalSamples;
	}
	
	private int getNumberOfNonErroredSamples(VitalsTimechart vitals)	{
		int totalSamples = 0;
		for (int i = 0; i < vitals.getCount().size(); i++) {
			totalSamples = totalSamples + vitals.getCount().get(i);
			totalSamples = totalSamples - vitals.getErrors().get(i);
		}
		return totalSamples;
	}
	
	private int getNumberOfErrors(VitalsTimechart vitals)	{
		int totalErrors = 0;
		for (int i = 0; i < vitals.getCount().size(); i++) {
			totalErrors = totalErrors + vitals.getErrors().get(i);
		}
		return totalErrors;
	}

	private double calculateApdexScore(LatencyHistogram histogram, VitalsTimechart vitals)	{
		double apdexScore = 0;
		int toleratingCount = 0;
		int satisfiedCount = 0;
		
		for (int i=TOLERATING_UPPER_BIN ; i<=TOLERATING_LOWER_BIN ; i++)	{
			toleratingCount = toleratingCount + histogram.getCount().get(i);
		}
		for (int i=SATISFIED_UPPER_BIN; i<=SATISFIED_LOWER_BIN; i++)	{
			satisfiedCount = satisfiedCount + histogram.getCount().get(i);
		}
	
		int totalSamples = getNumberOfSamples(vitals);	
//		System.out.println("total samples: " + totalSamples + ",tolerating: " + toleratingCount + ",satisfied: " + satisfiedCount);
		apdexScore = (satisfiedCount + (toleratingCount/2));
		apdexScore = apdexScore / totalSamples;
	    DecimalFormat newFormat = new DecimalFormat("#.##");
	    apdexScore =  Double.valueOf(newFormat.format(apdexScore));
	    return apdexScore;
	}
	// *******************************
	// * Specialized methods
	// *******************************
	
    public NotSoFastTaskMeasurement(String application, String task)	{
    	//long snap = FIFTEEN_MINUTES;
    	//long span = FOUR_HOURS;
    	long snap = ONE_SECOND;
    	long span = SIXTEEN_SECONDS;
    	
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
    
	
	private void appendTimechartMetrics(long startTime, long endTime, long snap) {
		ArrayList<Long> timeSequence = singleApdexTimechart.getTimeSequence();
		
//		System.out.println("singleApdexTimechart startTime=" + startTime + ", endTime=" + endTime + ", snap=" + snap);
		// Add APDEX scores for missing buckets within range
		for (long i = startTime ; i < endTime ; i+=snap)	{
			// Add only timeSequence data not yet populated 
			if ( (timeSequence.size() > 0 // timeSequence has data AND i is higher than last timeSequence
					&& i > timeSequence.get(timeSequence.size()-1)) 
					|| timeSequence.size() == 0)	{ // OR if timeSequence empty
				
				
//				System.out.println("singleApdexTimechart adding i=" + i);
				// APDEX score
				timeSequence.add(i);
				// apdexScore to be random value between POOR and FAIR
				double apdexScore = randDouble(apdexLowerLimit, apdexUpperLimit);
				singleApdexTimechart.getApdexScores().add(apdexScore);
				
				// Vitals
				vitalsTimechart.getTimeSequence().add(i);
				vitalsTimechart.getCount().add(randInt(countLowerLimit,countUpperLimit));
				vitalsTimechart.getErrors().add(randInt(errorLowerLimit,errorUpperLimit));
				vitalsTimechart.getP95().add((int) ((1-apdexScore)*1000)+150);
			}
			
		}
//		System.out.println("singleApdexTimechart size =" +  timeSequence.size());
		
	}

	private void createLatencyHistogramBinsAndRttZones(LatencyHistogram histogram) {
		ArrayList<String> bins = new ArrayList<String>();
		ArrayList<String> rttZones = new ArrayList<String>();
		bins.add("> " + Integer.toString(rttF));
		rttZones.add("Frustrated");
		int step = rttT/latencyHistogramFactor ;
		for (int from=rttF-step ; from >= 0 ; from=from-step)	{
			// the 3rd and subsequent bins 'to' should be decremented by one 
			// ["> 1200", "1125-1200", "1050-1124"]
			int to = from+step-1;
			
			if ((from+step) == rttF) { // 2nd bin
				to = from+step;
			}
			bins.add(Integer.toString(from)+"-"+Integer.toString(to));
			if (to >= rttT)	{
				rttZones.add("Tolerating");
			}
			else	{
				rttZones.add("Satisfied");
			}
		}
		histogram.setBins(bins);
		histogram.setRttZone(rttZones);
	}
	
	private void createLatencyHistogramCounts(LatencyHistogram histogram, VitalsTimechart vitals) {
		ArrayList<Integer> count = new ArrayList<Integer>();
		for (int i = 0; i<histogram.getBins().size() ; i++)	{
			count.add(0);
		}
		histogram.setCount(count);
		
    	// Calculate total samples
		int totalSamples = getNumberOfNonErroredSamples(vitals);
		// 5% of count to go to bins above p95 biased towards lower bins
		int aboveP95Count = (int) (totalSamples * 0.05);
		int divisor = 0;
		for (int i=TOLERATING_UPPER_BIN ; i<TOLERATING_LOWER_BIN ; i++)	{
			divisor = divisor + i;
		}
		for (int i=TOLERATING_UPPER_BIN ; i<TOLERATING_LOWER_BIN ; i++)	{
			histogram.getCount().set(i, aboveP95Count*i/divisor);
		}
		// ~40% to go into first tolerating bin
		double firstToleratingBinPercent = randDouble(0.38,0.42);
		histogram.getCount().set(TOLERATING_LOWER_BIN, (int) (totalSamples * firstToleratingBinPercent));
    	// ~55% to go into last satisfied bin
		
		histogram.getCount().set(SATISFIED_UPPER_BIN, (int) (totalSamples * (0.95-firstToleratingBinPercent)));
	
//		System.out.println("apdexScore: " + calculateApdexScore(histogram, vitals));
	}
	
    private void appendLatencyHistogram(long startTime, long endTime, long snap) {
    	// Create empty bins
    	createLatencyHistogramBinsAndRttZones(latencyHistogram);
    	createLatencyHistogramCounts(latencyHistogram, vitalsTimechart);
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
