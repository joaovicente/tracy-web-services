package com.apm4all.tracy.apimodel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;

@ApiModel(description = "Provides the Application measurement")
public interface TaskAnalysis {

	@ApiModelProperty(value = "The Appliation under analysis", required = true)
	String getApplication();

	@ApiModelProperty(value = "The Task under analysis", required = true)
	String getTask();

	@ApiModelProperty(value = "The Tracy records for this task page (needs expansion - composite object)", required = true)
	HashMap<String, Object> getTracyTasksPage();

	@ApiModelProperty(value = "The earliest time of the analysis window", required = true)
	long getEarliest();

	@ApiModelProperty(value = "The latest time of the analysis window", required = true)
	long getLatest();

	@ApiModelProperty(value = "The filter used for the analysis", required = true)
	String getFilter();

	@ApiModelProperty(value = "The (csv) fields to sort Tracy by", required = true)
	String getSort();

}