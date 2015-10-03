package com.apm4all.tracy;

public class TaskMeasurementService {
	// TaskMeasurementService receives TimeboxedRawTaskMeasurement(s) 
	// and converts them into TaskMeasurement(s)
	
	
	//FIXME: Inject cache singleton using Spring
    private RawTaskMeasurementCache rawTaskMeasurementCache = new RawTaskMeasurementCache();
	private TaskConfigDao taskConfigDao = new TaskConfigDao();

	/**
     * Gets a measurement for a given application, task
     *
     * @param application the application name
     * @param task the task name
     * @return the measurement, or <tt>null</tt> if the application or task are invalid 
     */
    public TaskMeasurement getTaskMeasurement(String application, String task) {
    	TaskMeasurement taskMeasurement = rawTaskMeasurementCache.getRawMeasurement(application, task);
    	
//    	TaskMeasurement taskMeasurement = null;
//    	if (task.equals("Not-so-fast"))	{
//    		taskMeasurement = new NotSoFastTaskMeasurement(application, task);
//    	}
//    	else if (task.equals("Static"))	{
//    		taskMeasurement = new StaticTaskMeasurement(application, task);
//    	}
    	return taskMeasurement;
    }

	public void setMeasurementCache(RawTaskMeasurementCache cache) {
		this.rawTaskMeasurementCache = cache;
	}

	public void refreshCache() {
		for (TaskConfig taskConfig : taskConfigDao.getAll()) {
			if (rawTaskMeasurementCache.isUpdateDue(taskConfig)) {
				RawTaskMeasurementCollector collector = 
						new RawTaskMeasurementSimulatedBadNotSoFastCollector(taskConfig, rawTaskMeasurementCache);
				collector.collect();
			}
		}
	}
}
