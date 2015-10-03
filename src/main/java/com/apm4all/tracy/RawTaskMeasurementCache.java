package com.apm4all.tracy;

import java.util.ArrayList;
import java.util.List;

public class RawTaskMeasurementCache {
	//TODO: Each Task to have a RawTaskMeasurementSequence: TreeMap<earliest, RawTaskMeasurementSequence> 
	//TODO: Each sequenced to be updatable via rawTaskMeasurementCache.add(app, task, timeboxedRawTaskMeasurement)
	//TODO: Each sequenced to be retrieved via rawTaskMeasurementCache.getTimeboxedRawTaskMeasurementSequence(app, task)
	//TODO: Will need to retrieve a list of RawTaskMeasurementSequence(s) for all Tasks in a given app (which should also contain link to taskConfig to extract rttT)
	//TODO: Each Task to have a TreeMap<earliest, TimeboxedRawTaskMeasurement> 

	List<RawTaskMeasurementSequence> sequences = new ArrayList<RawTaskMeasurementSequence>(); 
	
	/**
	 * Obtains Task measurement cached data for the chosen application tasks 
	 * @param application
	 * @param task
	 * @return 
	 */
	public TaskMeasurement getRawMeasurement(String application, String task) {
		//FIXME: Refactor to get measurement from cache instead
		return new StaticTaskMeasurement(application, task);
	}

	public boolean isUpdateDue(TaskConfig taskConfig) {
		RawTaskMeasurementSequence sequence = retrieveSequence(taskConfig);
		boolean due = (System.currentTimeMillis() > sequence.getLastestTime() + taskConfig.getLagInMsec() + taskConfig.getLagInMsec());
		return due;
	}

	private RawTaskMeasurementSequence retrieveSequence(TaskConfig taskConfig) {
		RawTaskMeasurementSequence foundSequence = null;
		for (RawTaskMeasurementSequence iSequence : sequences) {
			if (taskConfig.getApplication() == iSequence.getApplication() 
					&& taskConfig.getTask() == iSequence.getTask()) {
				foundSequence = iSequence;
			}
		}
		if (null == foundSequence) {
			// Create one if it does not exist
			foundSequence = new RawTaskMeasurementSequence(taskConfig);
			sequences.add(foundSequence);
		}
		return foundSequence;
	}
	
	public void storeTimeboxedRawTaskMeasurement(TaskConfig taskConfig, TimeboxedRawTaskMeasurement measurement) {
		RawTaskMeasurementSequence sequence = retrieveSequence(taskConfig);
		sequence.append(measurement.getTime(), measurement);
	}

	public void getSequence(TaskConfig taskConfig) {
		retrieveSequence(taskConfig);
	}
	
}
