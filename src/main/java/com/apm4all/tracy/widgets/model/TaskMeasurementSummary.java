package com.apm4all.tracy.widgets.model;

public class TaskMeasurementSummary {
	private String task;
	private Long lastUpdateTimestamp;
	private Integer period; // period captured by this measurement (also see periodUnit)
	// TODO: Convert to enum and test if supported by Camel REST And Swagger
	private String periodUnit; // Supports "ms", "s", "m", "h", "d" 
	// TODO: Convert to enum and test if supported by Camel REST And Swagger
	private String rttUnit;  // Supports "ms", "s", "m", "h", "d" 
	private Double apdexScore;
	private Integer rttT; // Response Time Threshold - Tolerating
	private Integer rttF; // Response Time Threshold - Frustrated
	private Double meanThroughputMetric; // The throughput mean value per unit specified
	// TODO: Convert to enum and test if supported by Camel REST And Swagger
	private String meanThroughputUnit; // Transactions Per Hour/Minute/Second ("TPS", "TPM", "TPH")
	private Double errorPercentage; // Percentage of errors (5xx status only). Display 0.00 if less than 0.005
	private Integer invocations; // Number of invocations in the period
	private Integer status2xx; //Number of invocations returning 2xx status  
	private Integer status3xx; //Number of invocations returning 3xx status  
	private Integer status4xx; //Number of invocations returning 4xx status  
	private Integer status5xx; //Number of invocations returning 5xx status  
	private Double meanLatency;
	private Double p50Latency;
	private Double p90Latency;
	private Double p95Latency;
	private Double p99Latency;

	public Long getLastUpdateTimestamp() {
		return lastUpdateTimestamp;
	}
	public void setLastUpdateTimestamp(Long lastUpdateTimestamp) {
		this.lastUpdateTimestamp = lastUpdateTimestamp;
	}
	public Integer getPeriod() {
		return period;
	}
	public void setPeriod(Integer period) {
		this.period = period;
	}
	public String getPeriodUnit() {
		return periodUnit;
	}
	public void setPeriodUnit(String periodUnit) {
		this.periodUnit = periodUnit;
	}
	public String getRttUnit() {
		return rttUnit;
	}
	public void setRttUnit(String rttUnit) {
		this.rttUnit = rttUnit;
	}
	public Integer getRttT() {
		return rttT;
	}
	public void setRttT(Integer rttT) {
		this.rttT = rttT;
	}
	public Integer getRttF() {
		return rttF;
	}
	public void setRttF(Integer rttF) {
		this.rttF = rttF;
	}
	public Double getMeanThroughputMetric() {
		return meanThroughputMetric;
	}
	public void setMeanThroughputMetric(Double meanThroughputMetric) {
		this.meanThroughputMetric = meanThroughputMetric;
	}
	public String getMeanThroughputUnit() {
		return meanThroughputUnit;
	}
	public void setMeanThroughputUnit(String meanThroughputUnit) {
		this.meanThroughputUnit = meanThroughputUnit;
	}
	public Double getErrorPercentage() {
		return errorPercentage;
	}
	public void setErrorPercentage(Double errorPercentage) {
		this.errorPercentage = errorPercentage;
	}
	public Integer getInvocations() {
		return invocations;
	}
	public void setInvocations(Integer invocations) {
		this.invocations = invocations;
	}
	public Integer getStatus2xx() {
		return status2xx;
	}
	public void setStatus2xx(Integer status2xx) {
		this.status2xx = status2xx;
	}
	public Integer getStatus3xx() {
		return status3xx;
	}
	public void setStatus3xx(Integer status3xx) {
		this.status3xx = status3xx;
	}
	public Integer getStatus4xx() {
		return status4xx;
	}
	public void setStatus4xx(Integer status4xx) {
		this.status4xx = status4xx;
	}
	public Integer getStatus5xx() {
		return status5xx;
	}
	public void setStatus5xx(Integer status5xx) {
		this.status5xx = status5xx;
	}
	public Double getMeanLatency() {
		return meanLatency;
	}
	public void setMeanLatency(Double meanLatency) {
		this.meanLatency = meanLatency;
	}
	public Double getP50Latency() {
		return p50Latency;
	}
	public void setP50Latency(Double p50Latency) {
		this.p50Latency = p50Latency;
	}
	public Double getP90Latency() {
		return p90Latency;
	}
	public void setP90Latency(Double p90Latency) {
		this.p90Latency = p90Latency;
	}
	public Double getP95Latency() {
		return p95Latency;
	}
	public void setP95Latency(Double p95Latency) {
		this.p95Latency = p95Latency;
	}
	public Double getP99Latency() {
		return p99Latency;
	}
	public void setP99Latency(Double p99Latency) {
		this.p99Latency = p99Latency;
	}
	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}
	public Double getApdexScore() {
		return apdexScore;
	}
	public void setApdexScore(Double apdexScore) {
		this.apdexScore = apdexScore;
	}
}
