package com.apm4all.tracy.simulations;

import java.text.DecimalFormat;
import java.util.Arrays;

import com.apm4all.tracy.measurement.application.ApplicationMeasurement;
import com.apm4all.tracy.widgets.model.MultiApdexTimechart;
import com.apm4all.tracy.widgets.model.SingleApdexTimechart;
import com.apm4all.tracy.widgets.model.TaskMeasurementSummary;
import com.apm4all.tracy.widgets.model.TasksSnapMeasurementSummary;
import com.apm4all.tracy.widgets.model.TasksSpanMeasurementSummary;

public class StaticApplicationMeasurement implements ApplicationMeasurement  {
	
	public StaticApplicationMeasurement(String application) {
	}

	private Double roundDouble(double d) {
    	DecimalFormat newFormat = new DecimalFormat("#.##");
    	double rd =  Double.valueOf(newFormat.format(d));
    	return new Double(rd);
	}
	
	private SingleApdexTimechart createSingleApdexTimechart(String task, double o) {
		SingleApdexTimechart single = new SingleApdexTimechart();
		
		single.setApplication("Static");
		single.setTask(task);
		single.setRttUnit("ms");
		single.setRttT(180);
		single.setRttF(720);
    	// APDEX timechart
		single.setTimeSequence(Arrays.asList(new Long[]
    			{1443985200000L, 1443986100000L, 1443987000000L, 1443987900000L, 
    			1443988800000L, 1443989700000L, 1443990600000L, 1443991500000L, 
    			1443992400000L, 1443993300000L, 1443994200000L, 1443995100000L, 
    			1443996000000L, 1443996900000L, 1443997800000L, 1443998700000L}
    			));
		single.setApdexScores(Arrays.asList(new Double[]
    			{roundDouble(0.99+o),
				roundDouble(0.98+o),
				roundDouble(0.99+o),
				roundDouble(0.93+o),
				roundDouble(0.94+o),
				roundDouble(0.97+o),
				roundDouble(0.95+o),
				roundDouble(0.97+o),
				roundDouble(0.83+o),
				roundDouble(0.93+o),
				roundDouble(0.97+o),
				roundDouble(0.94+o),
				roundDouble(0.96+o),
				roundDouble(0.98+o),
				roundDouble(0.95+o),
				roundDouble(0.96+o)}
    			));	
		return single;
	}
	
	
	@Override
	public MultiApdexTimechart getmultiApdexTimechart() {
		MultiApdexTimechart multi = new MultiApdexTimechart();
		multi.add(createSingleApdexTimechart("sa", 0.00));
		multi.add(createSingleApdexTimechart("sb", -0.10));
		multi.add(createSingleApdexTimechart("sc", -0.20));
		multi.add(createSingleApdexTimechart("sd", -0.30));
		return multi;
	}

	@Override
	public TasksSpanMeasurementSummary getTasksSpanMeasurementSummary() {
		TasksSpanMeasurementSummary tasks = new TasksSpanMeasurementSummary();
		TaskMeasurementSummary task = new TaskMeasurementSummary();
	
		task.setTask("Static");
		task.setErrorPercentage(0.01);
		task.setLastUpdateTimestamp(1443999599999L);
		task.setPeriod(4); 
		task.setPeriodUnit("h"); // Supports "ms", "s", "m", "h", "d" 
		task.setRttUnit("ms");  // Supports "ms", "s", "m", "h", "d" 
		task.setRttT(300); // Response Time Threshold - Tolerating
		task.setRttF(1200); // Response Time Threshold - Frustrated
		task.setMeanThroughputMetric(20.0) ; // The throughput mean value per unit specified
		task.setMeanThroughputUnit("TPM"); // Transactions Per Hour/Minute/Second ("TPS", "TPM", "TPH")
		task.setErrorPercentage(0.02); // Percentage of errors (5xx status only). Display 0.00 if less than 0.005
		task.setInvocations(4800); // Number of invocations in the period
		task.setStatus2xx(4795); //Number of invocations returning 2xx status  
		task.setStatus3xx(0); //Number of invocations returning 3xx status  
		task.setStatus4xx(4); //Number of invocations returning 4xx status  
		task.setStatus5xx(1); //Number of invocations returning 5xx status  
		task.setMeanLatency(100.0);
		task.setP50Latency(102.0);
		task.setP90Latency(130.0);
		task.setP95Latency(135.0);
		task.setP99Latency(200.0);

		// Simulate an Application with 4 tasks
		tasks.add(task);
		tasks.add(task);
		tasks.add(task);
		tasks.add(task);
		return tasks;
	}

	@Override
	public TasksSnapMeasurementSummary getTasksSnapMeasurementSummary() {
		TasksSnapMeasurementSummary tasks = new TasksSnapMeasurementSummary();
		TaskMeasurementSummary task = new TaskMeasurementSummary();
	
		task.setTask("Static");
		task.setErrorPercentage(0.01);
		task.setLastUpdateTimestamp(1443999599999L);
		task.setPeriod(15); 
		task.setPeriodUnit("m"); // Supports "ms", "s", "m", "h", "d" 
		task.setRttUnit("ms");  // Supports "ms", "s", "m", "h", "d" 
		task.setRttT(300); // Response Time Threshold - Tolerating
		task.setRttF(1200); // Response Time Threshold - Frustrated
		task.setMeanThroughputMetric(16.3) ; // The throughput mean value per unit specified
		task.setMeanThroughputUnit("TPM"); // Transactions Per Hour/Minute/Second ("TPS", "TPM", "TPH")
		task.setErrorPercentage(0.02); // Percentage of errors (5xx status only). Display 0.00 if less than 0.005
		task.setInvocations(245); // Number of invocations in the period
		task.setStatus2xx(240); //Number of invocations returning 2xx status  
		task.setStatus3xx(0); //Number of invocations returning 3xx status  
		task.setStatus4xx(4); //Number of invocations returning 4xx status  
		task.setStatus5xx(1); //Number of invocations returning 5xx status  
		task.setMeanLatency(99.0);
		task.setP50Latency(101.0);
		task.setP90Latency(129.0);
		task.setP95Latency(145.0);
		task.setP99Latency(180.0);

		// Simulate an Application with 4 tasks
		tasks.add(task);
		tasks.add(task);
		tasks.add(task);
		tasks.add(task);
		return tasks;
	}
}
