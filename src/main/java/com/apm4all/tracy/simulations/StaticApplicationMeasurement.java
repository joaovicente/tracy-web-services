package com.apm4all.tracy.simulations;

import java.text.DecimalFormat;
import java.util.Arrays;

import com.apm4all.tracy.apimodel.ApplicationMeasurement;
import com.apm4all.tracy.apimodel.MultiApdexTimechart;
import com.apm4all.tracy.apimodel.SingleApdexTimechart;
import com.apm4all.tracy.apimodel.TaskMeasurementSummary;
import com.apm4all.tracy.apimodel.TasksSnapMeasurementSummary;
import com.apm4all.tracy.apimodel.TasksSpanMeasurementSummary;

public class StaticApplicationMeasurement implements ApplicationMeasurement  {
	private final double APDEX_EXCELLENT = 0.99;
	private final double APDEX_GOOD = 0.90;
	private final double APDEX_FAIR = 0.75;
	private final double APDEX_POOR = 0.60;
	private final double APDEX_UNACCEPTABLE = 0.40;
	
	public StaticApplicationMeasurement(String application) {
	}

	private Double roundDouble(double d) {
    	DecimalFormat newFormat = new DecimalFormat("#.##");
    	double rd =  Double.valueOf(newFormat.format(d));
    	return new Double(rd);
	}
	
	@Override
	public MultiApdexTimechart getMultiApdexTimechart() {
		MultiApdexTimechart multi = new MultiApdexTimechart();
		multi.add(new StaticTaskMeasurement("demo-static-app", "excellent-task").getSingleApdexTimechart());
		multi.add(new StaticTaskMeasurement("demo-static-app", "good-task").getSingleApdexTimechart());
		multi.add(new StaticTaskMeasurement("demo-static-app", "fair-task").getSingleApdexTimechart());
		multi.add(new StaticTaskMeasurement("demo-static-app", "poor-task").getSingleApdexTimechart());
		multi.add(new StaticTaskMeasurement("demo-static-app", "unacceptable-task").getSingleApdexTimechart());
		return multi;
	}

	private void setResponseTimeStats(TaskMeasurementSummary task, double mean, double p50, double p90, double p95, double p99) {
		task.setMeanLatency(mean);
		task.setP50Latency(p50);
		task.setP90Latency(p90);
		task.setP95Latency(p95);
		task.setP99Latency(p99);
	}
	
	private void fillResponseTimeStats(TaskMeasurementSummary task) {
		// Assumes task.apdexScore has been populated 
		//mean, median, p90, p95, p99
		if (task.getApdexScore() == APDEX_EXCELLENT)	{
			// Satisfied: 100%, Tolerating: 0%, Frustrated: 0%
			setResponseTimeStats(task, 115.0, 140.0, 150.0, 160.0, 170.0);
		}
		else if (task.getApdexScore() == APDEX_GOOD)	{
			// Satisfied: 80%, Tolerating: 20%, Frustrated: 0%
			setResponseTimeStats(task, 130.0, 140.0, 200.0, 250.0, 300);
		}
		else if (task.getApdexScore() == APDEX_FAIR)	{
			// Satisfied: 50%, Tolerating: 50%, Frustrated: 0%
			setResponseTimeStats(task, 192.0, 200.0, 300.0, 400.0,	500.0);
		}
		else if (task.getApdexScore() == APDEX_POOR)	{
			// Satisfied: 40%, Tolerating: 60%, Frustrated: 0%
			setResponseTimeStats(task, 242.0, 250.0, 400.0, 500.0,	600.0);
		}
		else if (task.getApdexScore() == APDEX_UNACCEPTABLE)	{
			// Satisfied: 0%, Tolerating: 80%, Frustrated: 20%
			setResponseTimeStats(task, 472.0, 500.0, 800.0, 900.0,	1000.0);
		}
	}
	
	private void fillTaskSpanMeasurementSummary(TaskMeasurementSummary task, String taskName, Double apdexScore) {
		task.setTask(taskName);
		task.setErrorPercentage(0.01);
		task.setLastUpdateTimestamp(1443999599999L);
		task.setPeriod(4); 
		task.setPeriodUnit("h"); // Supports "ms", "s", "m", "h", "d" 
		task.setRttUnit("ms");  // Supports "ms", "s", "m", "h", "d" 
		task.setApdexScore(apdexScore);
		task.setRttT(180); // Response Time Threshold - Tolerating
		task.setRttF(720); // Response Time Threshold - Frustrated
		task.setMeanThroughputMetric(20.0) ; // The throughput mean value per unit specified
		task.setMeanThroughputUnit("TPM"); // Transactions Per Hour/Minute/Second ("TPS", "TPM", "TPH")
		task.setErrorPercentage(0.02); // Percentage of errors (5xx status only). Display 0.00 if less than 0.005
		task.setInvocations(4800); // Number of invocations in the period
		task.setStatus2xx(4795); //Number of invocations returning 2xx status  
		task.setStatus3xx(0); //Number of invocations returning 3xx status  
		task.setStatus4xx(4); //Number of invocations returning 4xx status  
		task.setStatus5xx(1); //Number of invocations returning 5xx status  
		fillResponseTimeStats(task);
	}
	
	private void fillTaskSnapMeasurementSummary(TaskMeasurementSummary task, String taskName, Double apdexScore) {
		task.setTask(taskName);
		task.setErrorPercentage(0.01);
		task.setLastUpdateTimestamp(1443999599999L);
		task.setPeriod(15); 
		task.setPeriodUnit("m"); // Supports "ms", "s", "m", "h", "d" 
		task.setRttUnit("ms");  // Supports "ms", "s", "m", "h", "d" 
		task.setApdexScore(apdexScore);
		task.setRttT(180); // Response Time Threshold - Tolerating
		task.setRttF(720); // Response Time Threshold - Frustrated
		task.setMeanThroughputMetric(16.3) ; // The throughput mean value per unit specified
		task.setMeanThroughputUnit("TPM"); // Transactions Per Hour/Minute/Second ("TPS", "TPM", "TPH")
		task.setErrorPercentage(0.02); // Percentage of errors (5xx status only). Display 0.00 if less than 0.005
		task.setInvocations(245); // Number of invocations in the period
		task.setStatus2xx(240); //Number of invocations returning 2xx status  
		task.setStatus3xx(0); //Number of invocations returning 3xx status  
		task.setStatus4xx(4); //Number of invocations returning 4xx status  
		task.setStatus5xx(1); //Number of invocations returning 5xx status  
		fillResponseTimeStats(task);
	}
	
	@Override
	public TasksSpanMeasurementSummary getTasksSpanMeasurementSummary() {
		TasksSpanMeasurementSummary tasks = new TasksSpanMeasurementSummary();
		
		TaskMeasurementSummary task = new TaskMeasurementSummary();
		fillTaskSpanMeasurementSummary(task, "excellent-task", APDEX_EXCELLENT);
		tasks.add(task);
		
		task = new TaskMeasurementSummary();
		fillTaskSpanMeasurementSummary(task, "good-task", APDEX_GOOD);
		tasks.add(task);
		
		task = new TaskMeasurementSummary();
		fillTaskSpanMeasurementSummary(task, "fair-task", APDEX_FAIR);
		tasks.add(task);
		
		task = new TaskMeasurementSummary();
		fillTaskSpanMeasurementSummary(task, "poor-task", APDEX_POOR);
		tasks.add(task);
		
		task = new TaskMeasurementSummary();
		fillTaskSnapMeasurementSummary(task, "unacceptable-task", APDEX_UNACCEPTABLE);
		tasks.add(task);
		return tasks;
	}

	
	@Override
	public TasksSnapMeasurementSummary getTasksSnapMeasurementSummary() {
		TasksSnapMeasurementSummary tasks = new TasksSnapMeasurementSummary();
		
		TaskMeasurementSummary task = new TaskMeasurementSummary();
		fillTaskSnapMeasurementSummary(task, "excellent-task", APDEX_EXCELLENT);
		tasks.add(task);
		
		task = new TaskMeasurementSummary();
		fillTaskSnapMeasurementSummary(task, "good-task", APDEX_GOOD);
		tasks.add(task);
		
		task = new TaskMeasurementSummary();
		fillTaskSnapMeasurementSummary(task, "fair-task", APDEX_FAIR);
		tasks.add(task);
		
		task = new TaskMeasurementSummary();
		fillTaskSnapMeasurementSummary(task, "poor-task", APDEX_POOR);
		tasks.add(task);
		
		task = new TaskMeasurementSummary();
		fillTaskSnapMeasurementSummary(task, "unacceptable-task", APDEX_UNACCEPTABLE);
		tasks.add(task);
		return tasks;
	}
}
