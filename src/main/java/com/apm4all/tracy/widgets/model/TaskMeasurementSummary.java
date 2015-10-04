package com.apm4all.tracy.widgets.model;

public class TaskMeasurementSummary {
	private Long lastUpdateTimestamp;
	private Integer period; // period captured by this measurement (also see periodUnit)
	// TODO: Convert to enum and test if supported by Camel REST And Swagger
	private String periodUnit; // Supports "ms", "s", "m", "h", "d" 
	// TODO: Convert to enum and test if supported by Camel REST And Swagger
	private String rttUnit;  // Supports "ms", "s", "m", "h", "d" 
	private Integer rttT; // Response Time Threshold - Tolerating
	private Integer rttF; // Response Time Threshold - Frustrated
	private Double meanThroughputMetric; // The throughput mean value per unit specified
	// TODO: Convert to enum and test if supported by Camel REST And Swagger
	private String meanThroughputUnit; // Transactions Per Hour/Minute/Second ("TPS", "TPM", "TPH")
	private Double errorPercentage; // Percentage of errors (5xx status only). Display 0.00 if less than 0.005
	private Long invocations; // Number of invocations in the period
	private Long status2xx; //Number of invocations returning 2xx status  
	private Long status3xx; //Number of invocations returning 3xx status  
	private Long status4xx; //Number of invocations returning 4xx status  
	private Long status5xx; //Number of invocations returning 5xx status  
	private Double meanLatency;
	private Double p50Latency;
	private Double p90Latency;
	private Double p95Latency;
	private Double p99Latency;
}
