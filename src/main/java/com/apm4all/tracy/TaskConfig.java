package com.apm4all.tracy;

public class TaskConfig {
	public enum CollectorType {SIMULATED, ELASTIC_SEARCH};
	private CollectorType collectorType;;
	private String application;
	private String task;

	public TaskConfig(String app, String task) {
		// TODO Auto-generated constructor stub
	}

	public CollectorType getCollectorType() {
		return collectorType;
	}

	public void setCollectorType(CollectorType collectorType) {
		this.collectorType = collectorType;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public Long getLagInMsec() {
		// TODO Auto-generated method stub
		return null;
	}

}
