package com.apm4all.tracy;

import java.util.ArrayList;
import java.util.List;

public class TaskConfigDao {
	List<TaskConfig> taskConfigs;
	
	public TaskConfigDao()	{
		taskConfigs = new ArrayList<TaskConfig>();
		
		// Create static task config
		TaskConfig simStaticConfig = new TaskConfig("Simulated", "Static");
		simStaticConfig.setCollectorType(TaskConfig.CollectorType.SIMULATED);
		taskConfigs.add(simStaticConfig);

		// Create static task config
		TaskConfig simBadNotSoFastConfig = new TaskConfig("SimulatedBad", "NotSoFast");
		simBadNotSoFastConfig.setCollectorType(TaskConfig.CollectorType.SIMULATED);
		taskConfigs.add(simBadNotSoFastConfig);
	}
	
	public List<TaskConfig> getAll() {
		return taskConfigs;
	}
}
