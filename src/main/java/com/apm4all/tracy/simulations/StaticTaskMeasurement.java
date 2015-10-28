package com.apm4all.tracy.simulations;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.apm4all.tracy.measurement.task.TaskMeasurement;
import com.apm4all.tracy.widgets.model.LatencyHistogram;
import com.apm4all.tracy.widgets.model.SingleApdexTimechart;
import com.apm4all.tracy.widgets.model.VitalsTimechart;

public class StaticTaskMeasurement implements TaskMeasurement {
	// TODO: Create an Apdex class to hold constants, score calculations and the like
	private static final double APDEX_EXCELLENT_UL 		= 1.00;
	private static final double APDEX_EXCELLENT_LL 		= 0.94;
	private static final double APDEX_GOOD_UL 			= 0.93;
	private static final double APDEX_GOOD_LL 			= 0.85;
	private static final double APDEX_FAIR_UL 			= 0.84;
	private static final double APDEX_FAIR_LL 			= 0.70;
	private static final double APDEX_POOR_UL 			= 0.69;
	private static final double APDEX_POOR_LL 			= 0.50;
	private static final double APDEX_UNACCEPTABLE_UL 	= 0.49;
//	private static final double APDEX_UNACCEPTABLE_LL 	= 0.00;
	private SingleApdexTimechart singleApdexTimechart;
	private VitalsTimechart vitalsTimechart;
	private LatencyHistogram latencyHistogram;
	private String application;
	private String task;
	static Map<String, StaticTaskMeasurement> measurements = 
			new HashMap<String, StaticTaskMeasurement>();

    public StaticTaskMeasurement(String application, String task)	{
    	this.application = application;
    	this.task = task;
    	if (measurements.containsKey(task))	{
    		singleApdexTimechart = measurements.get(task).getSingleApdexTimechart();
    		vitalsTimechart = measurements.get(task).getVitalsTimechart();
    		latencyHistogram = measurements.get(task).getLatencyHistogram();
    	}
    	else	{
    		produceMeasurement();
    		measurements.put(task, this);
    	}
    }
  
    public double roundDouble(double toRound, int decPlaces)	{
    	StringBuilder sb = new StringBuilder();
    	sb.append("#");
    	if (decPlaces > 0)	{
    		sb.append(".");
    	}
    	for (int i=0 ; i < decPlaces ; i++)	{
    		sb.append("#");
    	}
	    DecimalFormat newFormat = new DecimalFormat(sb.toString());
	    double rounded =  Double.valueOf(newFormat.format(toRound));
	    return rounded;
    }
    
	public double randDouble(double min, double max) {
	    Random rand = new Random();
	    double randomValue = min + (max - min) * rand.nextDouble();
	    return roundDouble(randomValue,2);
	}
	
	private double produceP95() {
		double p95 = 0.0;
		if (this.task.contains("Excellent"))	{
			p95 = 160.0;
		}
		else if(this.task.contains("Good")){
			p95 = 250.0;
		}
		else if(this.task.contains("Fair")){
			p95 = 400.0;
		}
		else if(this.task.contains("Poor")){
			p95 = 500.0;
		}
		else if(this.task.contains("Unacceptable")){
			p95 = 800.0;
		}
		return p95;
	}
	
	public double midPointWithVariance(double lower, double upper) {
		double rangeWidth = upper-lower;
		double midPoint = upper-(rangeWidth/2);
		return(randDouble(midPoint-rangeWidth/8, midPoint+rangeWidth/8));
	}
	
	public static int randInt(int min, int max) {
	    Random rand = new Random();
	    return rand.nextInt((max - min) + 1) + min;
	}
	
	private double produceApdexScore()	{
		double apdexScore = 0.0;
		if (this.task.contains("Excellent"))	{
			apdexScore = midPointWithVariance(APDEX_EXCELLENT_LL, APDEX_EXCELLENT_UL);
		}
		else if(this.task.contains("Good")){
			apdexScore = midPointWithVariance(APDEX_GOOD_LL, APDEX_GOOD_UL);
		}
		else if(this.task.contains("Fair")){
			apdexScore = midPointWithVariance(APDEX_FAIR_LL, APDEX_FAIR_UL);
		}
		else if(this.task.contains("Poor")){
			apdexScore = midPointWithVariance(APDEX_POOR_LL, APDEX_POOR_UL);
		}
		else if(this.task.contains("Unacceptable")){
			apdexScore = midPointWithVariance(APDEX_UNACCEPTABLE_UL-0.10, APDEX_UNACCEPTABLE_UL);
		}
		return apdexScore;
	}

    private void latencyHistrogramSpread(ArrayList<Integer> histogramCounts, int invocations, int satisfiedPerc, int toleratingPerc, int frustratedPerc) {
    	final int FRUSTRATED_START_BUCKET 	= 0;
    	final int TOLERATING_START_BUCKET 	= 1;
    	final int SATISFIED_START_BUCKET 	= 13;
    	final int LAST_BUCKET 				= 16;
    	
    	final int FRUSTRATED_BUCKET_COUNT 	= 1;
    	final int TOLERATING_BUCKET_COUNT 	= 12;
    	final int SATISFIED_BUCKET_COUNT 	= 4;
    	// 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20
    	//  F  T  T  T  T  T  T  T  T  T  T  T  T  T  T  T  T  S  S  S  S
    	
    	int frustratedBucketCount = invocations * frustratedPerc /100 / FRUSTRATED_BUCKET_COUNT;
    	int toleratingBucketCount = invocations * toleratingPerc /100 / TOLERATING_BUCKET_COUNT;
    	int satisfiedBucketCount = invocations * satisfiedPerc /100 / SATISFIED_BUCKET_COUNT;
    	
    	for (int bucket=0 ; bucket <= LAST_BUCKET ; bucket++)	{
    		if (bucket >= FRUSTRATED_START_BUCKET && bucket < FRUSTRATED_START_BUCKET + FRUSTRATED_BUCKET_COUNT) {
    			histogramCounts.add(frustratedBucketCount);
    		}
    		if (bucket >= TOLERATING_START_BUCKET && bucket < TOLERATING_START_BUCKET + TOLERATING_BUCKET_COUNT) {
    			histogramCounts.add(toleratingBucketCount);
    		}
    		if (bucket >= SATISFIED_START_BUCKET && bucket < SATISFIED_START_BUCKET + SATISFIED_BUCKET_COUNT) {
    			histogramCounts.add(satisfiedBucketCount);
    		}
    	}
	}
	
	private List<Integer> produceLatencyHistogramCounts()	{
		ArrayList<Integer> counts;
		counts = new ArrayList<Integer>();

		// TODO: To be accurate should calculate total invocations minus errors 
    	int invocations = 400*16;
		if (this.task.contains("Excellent"))	{
			latencyHistrogramSpread(counts, invocations, 100, 0, 0);
		}
		else if(this.task.contains("Good")){
			latencyHistrogramSpread(counts, invocations, 80, 20, 0);
		}
		else if(this.task.contains("Fair")){
			latencyHistrogramSpread(counts, invocations, 50, 50, 0);
		}
		else if(this.task.contains("Poor")){
			latencyHistrogramSpread(counts, invocations, 40, 60, 0);
		}
		else if(this.task.contains("Unacceptable")){
			latencyHistrogramSpread(counts, invocations, 0, 80, 20);
		}    	
		return counts;
	}

	private void produceMeasurement()	{
		singleApdexTimechart = new SingleApdexTimechart();
    	singleApdexTimechart.setApplication(application);
    	singleApdexTimechart.setTask(task);
    	singleApdexTimechart.setRttUnit("ms");
    	singleApdexTimechart.setRttT(180);
    	singleApdexTimechart.setRttF(720);
    	final int numBuckets = 16;
    	
    	// APDEX timechart
    	Long[] timeSequence = new Long[]
    			{1443985200000L, 1443986100000L, 1443987000000L, 1443987900000L, 
    			1443988800000L, 1443989700000L, 1443990600000L, 1443991500000L, 
    			1443992400000L, 1443993300000L, 1443994200000L, 1443995100000L, 
    			1443996000000L, 1443996900000L, 1443997800000L, 1443998700000L};
    	singleApdexTimechart.setTimeSequence(Arrays.asList(timeSequence));
    	ArrayList<Double> apdexScores = new ArrayList<Double>();
    	for (int i=0  ; i < numBuckets ; i++)	{
    		apdexScores.add(produceApdexScore());
    	}
    	singleApdexTimechart.setApdexScores(apdexScores);
    	
    	// Vitals Timechart
    	vitalsTimechart = new VitalsTimechart();
    	vitalsTimechart.setTimeSequence(Arrays.asList(timeSequence));
    
    	ArrayList<Integer> counts = new ArrayList<Integer>();
    	for (int i=0  ; i < numBuckets ; i++)	{
    		counts.add(randInt(400, 405)); // around 400
    	}
    	vitalsTimechart.setCount(counts);
    
    	ArrayList<Integer> errors = new ArrayList<Integer>();
    	for (int i=0  ; i < numBuckets ; i++)	{
    		errors.add(randInt(0, 2)); // around 400
    	}
    	vitalsTimechart.setErrors(errors);
    
    	ArrayList<Double> p95s = new ArrayList<Double>();
    	double P95_VARIANCE = 1.10;
    	for (int i=0  ; i < numBuckets ; i++)	{
    		double p95 = produceP95();
    		p95s.add(roundDouble(randDouble(p95, p95*P95_VARIANCE),0)); // oscillate x%
    	}
    	vitalsTimechart.setP95(p95s);
    	
    	// Latency histogram
    	latencyHistogram = new LatencyHistogram();
    	latencyHistogram.setBins(Arrays.asList(new String[]
    			{">720", 
    			"675-719", "630-674", "585-629", "540-584", 
    			"495-539", "450-494", "405-449", "360-404", 
    			"315-359", "270-314", "225-269", "180-224", 
    			"135-179", "90-134", "45-89", "0-44"}
    	));
    	latencyHistogram.setRttZone(Arrays.asList(new String[]
    			{"Frustrated",
    			"Tolerating","Tolerating","Tolerating","Tolerating",
    			"Tolerating","Tolerating","Tolerating","Tolerating",
    			"Tolerating","Tolerating","Tolerating","Tolerating",
    			"Satisfied","Satisfied","Satisfied","Satisfied"}
    	));
    	
    	latencyHistogram.setCount(produceLatencyHistogramCounts());
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
