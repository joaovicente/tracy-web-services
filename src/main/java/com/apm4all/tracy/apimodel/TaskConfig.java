package com.apm4all.tracy.apimodel;

public class TaskConfig {
	static final String RTT_UNIT_MSEC = "ms";
	static final String RTT_UNIT_SEC = "s";
	static final String RTT_UNIT_HOUR = "h";
	static final String RTT_UNIT_DAY = "d";
	private String application;
	private String task;
	private String definingFilter = "component:\"hello-tracy\" AND label:\"outer\"";
	private TaskConfigMeasurement measurement = new TaskConfigMeasurement();

	public TaskConfig()	{ }
	
	public TaskConfig(String application, String task)	{
		this.application = application;
		this.task = task;
	}
	
	public static class TaskConfigMeasurement {
		private int span = 15 * 60 * 1000 ; // 15 minutes in msec
		private int snap = 1 * 60 * 1000; // 1 minute in msec
		private int lag = 0;
		private int rttTolerating = 200;
		private int rttFrustrated = 800;
		private String rttUnit = RTT_UNIT_MSEC;
		
		public int getSpan() {
			return span;
		}
		public void setSpan(int span) {
			this.span = span;
		}
		public int getSnap() {
			return snap;
		}
		public void setSnap(int snap) {
			this.snap = snap;
		}
		public int getLag() {
			return lag;
		}
		public void setLag(int lag) {
			this.lag = lag;
		}
		public int getRttTolerating() {
			return rttTolerating;
		}
		public void setRttTolerating(int rttTolerating) {
			this.rttTolerating = rttTolerating;
		}
		public int getRttFrustrated() {
			return rttFrustrated;
		}
		public void setRttFrustrated(int rttFrustrated) {
			this.rttFrustrated = rttFrustrated;
		}
		public String getRttUnit() {
			return this.rttUnit; 
		}
		public void setRttUnit(String rttUnit) {
			this.rttUnit = rttUnit; 
		}
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

	public TaskConfigMeasurement getMeasurement() {
		return measurement;
	}

	public void setMeasurement(TaskConfigMeasurement measurement) {
		this.measurement = measurement;
	}
	
	public String getDefiningFilter() {
		return definingFilter;
	}
}
