package com.apm4all.tracy.simulations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.apm4all.tracy.apimodel.TaskAnalysis;

public class TaskAnalysisFake implements TaskAnalysis {
	private long earliest;
	private long latest;
	private String filter;
	private String sort;
	private String application;
	private String task;
	private int offset;
	private int limit;
	private int records;
	// tracyTasks 1-has->* tracyTask 1-has->* tracyEvents
	private ArrayList<Object> tracyTasks;

	public TaskAnalysisFake(String application, String task, long earliest, long latest, String filter, String sort, int limit, int offset)	{
		// TODO: handle params: earliest, latest, filter, sort
		this.application = application;
		this.task = task;
		this.earliest = earliest;
		this.latest = latest;
		this.filter = filter;
		this.sort = sort;
		this.records = 18;
		this.offset = offset;
		this.limit = limit;
		this.tracyTasks = new ArrayList<Object>(200);
	}
	
	/* (non-Javadoc)
	 * @see com.apm4all.tracy.analysis.task.TaskAnalysis#getApplication()
	 */
	@Override
	public String getApplication()	{
		return this.application;
	}
	
	/* (non-Javadoc)
	 * @see com.apm4all.tracy.analysis.task.TaskAnalysis#getTask()
	 */
	@Override
	public String getTask()	{
		return this.task;
	}
	
	/* (non-Javadoc)
	 * @see com.apm4all.tracy.analysis.task.TaskAnalysis#getTracyTasksPage()
	 */
	@Override
	public HashMap<String,Object> getTracyTasksPage()	{
		//TODO: Return structure line below
		HashMap<String,Object> tracyTasksPage = new HashMap<String,Object>();
		tracyTasksPage.put("offset", this.offset);
		tracyTasksPage.put("limit", this.limit);
		tracyTasksPage.put("records", this.records);
		ArrayList<Object> tracyTasks = new ArrayList<Object>();
		tracyTasksPage.put("tracyTasks", tracyTasks);
	
		// Create mocked up tracyTasks
		for (int i=0 ; i<records ; i++)	{
			tracyTasks.add(generateTracyTask((latest-earliest)*i/limit, i));
		}
		return tracyTasksPage;
	}

	private HashMap<String, Object> generateTracyTask(long timeOffset, int sequenceNumber)	{
		// TODO: Consider a class hierarchy for TracyTask, 
		// tracyTasks[] 1-has->* tracyTask{} 1-has->* tracyEvents[]
		ArrayList<Object> tracyTaskEvents = new ArrayList<Object>(20);
		HashMap<String, Object> tracyEvents = new HashMap<String, Object>();
		HashMap<String, Object> tracyTask = new HashMap<String, Object>();
	    long rt = this.earliest;
	    
	    // Add jitter to offset
	    timeOffset += ThreadLocalRandom.current().nextInt(0, 1000 + 1);

	    //[675 TO 719]
	    String filterArray[] = this.filter.split(":");
//	    System.out.println(filterArray[1]);
	    String limits[] = filterArray[1].split("\\Q[\\E|\\Q]\\E| TO ");
//	    System.out.println(limits.length);
//	    for (int i=0 ; i < limits.length ; i++)	{
//	    	System.out.println(limits[i]);
//	    }
	    int ll = Integer.parseInt(limits[1]);
//	    int ul = Integer.parseInt(limits[2]);
	    
	    // long offset = 10; // msecOffset
	    // long offset = 1010; // secOffset
	    // long offset = 61010; // minOffset
	    long offset = 3601000L; // hourOffset
	    
	    if (this.application.contains("demo-static"))	{
	    	// msec unit
	    	offset = (ll/10L);
	    }
	    else	{
	    	// hour unit
	    	offset = ll*3601000L/10L;
	    }
	    
	    String host = "46.7.188.254";
	    // Progressive frame creation
	    for (int i=0 ; i<sequenceNumber ; i++)	{
	    	tracyTaskEvents.add(
	    			createTracyEvent(
	    					"TID-ab1234-x", 
	    					"AD24", 
	    					"layer" + Integer.toString(i), 
	    					"A00" + Integer.toString(i), 
	    					timeOffset+rt+offset*5, 
	    					timeOffset+rt+offset*7, 
	    					offset*2, 
	    					host, 
	    					"layer" + Integer.toString(i)));
	    }
	    tracyTaskEvents.add(createTracyEvent("TID-ab1234-x", "4F3D", "foo", "AD24", timeOffset+rt+offset*5, timeOffset+rt+offset*7, offset*2, host, "Service"));
	    tracyTaskEvents.add(createTracyEvent("TID-ab1234-x", "4F3D", "bar", "AE5F", timeOffset+rt+offset*3, timeOffset+rt+offset*5, offset*2, host, "Service"));
	    tracyTaskEvents.add(createTracyEvent("TID-ab1234-x", "23CF", "Http servlet", "4F3D", timeOffset+rt+offset*2, timeOffset+rt+offset*8, offset*6, host, "Service"));
	    tracyTaskEvents.add(createTracyEvent("TID-ab1234-x", "DBF5", "Service handler", "23CF", timeOffset+rt+offset, timeOffset+rt+offset*9, offset*8, host, "Proxy"));
	    tracyTaskEvents.add(createTracyEvent("TID-ab1234-x", "AAAA", "Client handler", "DBF5", timeOffset+rt, timeOffset+rt+offset*10, offset*10, host, "Proxy"));
	    tracyEvents.put("tracyEvents", tracyTaskEvents);
	    tracyTask.put("tracyTask", tracyEvents);
	    return tracyTask;
	}
	
	private HashMap<String, Object> createTracyEvent( 
			String taskId, 
			String parentOptId, 
			String label, 
			String optId, 
			Long msecBefore, 
			Long msecAfter, 
			Long msecElapsed, 
			String host, 
			String component)	{
		HashMap<String, Object> tracyEvent = new HashMap<String, Object>(10);
		tracyEvent.put("taskId", taskId);
		tracyEvent.put("parentOptId", parentOptId);
		tracyEvent.put("label", label);
		tracyEvent.put("optId", optId);
		tracyEvent.put("msecBefore", msecBefore);
		tracyEvent.put("msecAfter", msecAfter);
		tracyEvent.put("msecElapsed", msecElapsed);
		tracyEvent.put("host", host);
		tracyEvent.put("component", component);
		return tracyEvent;
	}

	/* (non-Javadoc)
	 * @see com.apm4all.tracy.analysis.task.TaskAnalysis#getEarliest()
	 */
	@Override
	public long getEarliest() {
		return earliest;
	}

	/* (non-Javadoc)
	 * @see com.apm4all.tracy.analysis.task.TaskAnalysis#getLatest()
	 */
	@Override
	public long getLatest() {
		return latest;
	}

	/* (non-Javadoc)
	 * @see com.apm4all.tracy.analysis.task.TaskAnalysis#getFilter()
	 */
	@Override
	public String getFilter() {
		return filter;
	}

	/* (non-Javadoc)
	 * @see com.apm4all.tracy.analysis.task.TaskAnalysis#getSort()
	 */
	@Override
	public String getSort() {
		return sort;
	}
}

//{
//	  "earliest": 1443985200000,
//	  "latest": 1443996900000,
//	  "filter": "msecElapsed:[14 TO 16]",
//	  "sort": "-msecElapsed",
//	  "application": "SimulatedBatchApp",
//	  "task": "BatchExcellentTask",
//	  "tracyTasksPage": {
//	    "limit": 20,
//	    "tracyTasks": [
//	      {
//	        "tracyTask": {
//	          "tracyEvents": [
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1444010407964,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 10082800,
//	              "msecAfter": 1444020490764,
//	              "component": "Service",
//	              "optId": "AD24",
//	              "label": "foo",
//	              "parentOptId": "4F3D"
//	            },
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1444000325164,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 10082800,
//	              "msecAfter": 1444010407964,
//	              "component": "Service",
//	              "optId": "AE5F",
//	              "label": "bar",
//	              "parentOptId": "4F3D"
//	            },
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1443995283764,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 30248400,
//	              "msecAfter": 1444025532164,
//	              "component": "Service",
//	              "optId": "4F3D",
//	              "label": "Http servlet",
//	              "parentOptId": "23CF"
//	            },
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1443990242364,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 40331200,
//	              "msecAfter": 1444030573564,
//	              "component": "Proxy",
//	              "optId": "23CF",
//	              "label": "Service handler",
//	              "parentOptId": "DBF5"
//	            },
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1443985200964,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 50414000,
//	              "msecAfter": 1444035614964,
//	              "component": "Proxy",
//	              "optId": "DBF5",
//	              "label": "Client handler",
//	              "parentOptId": "AAAA"
//	            }
//	          ]
//	        }
//	      },
//	      {
//	        "tracyTask": {
//	          "tracyEvents": [
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1444010992987,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 10082800,
//	              "msecAfter": 1444021075787,
//	              "component": "Service",
//	              "optId": "AD24",
//	              "label": "foo",
//	              "parentOptId": "4F3D"
//	            },
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1444000910187,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 10082800,
//	              "msecAfter": 1444010992987,
//	              "component": "Service",
//	              "optId": "AE5F",
//	              "label": "bar",
//	              "parentOptId": "4F3D"
//	            },
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1443995868787,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 30248400,
//	              "msecAfter": 1444026117187,
//	              "component": "Service",
//	              "optId": "4F3D",
//	              "label": "Http servlet",
//	              "parentOptId": "23CF"
//	            },
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1443990827387,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 40331200,
//	              "msecAfter": 1444031158587,
//	              "component": "Proxy",
//	              "optId": "23CF",
//	              "label": "Service handler",
//	              "parentOptId": "DBF5"
//	            },
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1443985785987,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 50414000,
//	              "msecAfter": 1444036199987,
//	              "component": "Proxy",
//	              "optId": "DBF5",
//	              "label": "Client handler",
//	              "parentOptId": "AAAA"
//	            }
//	          ]
//	        }
//	      },
//	      {
//	        "tracyTask": {
//	          "tracyEvents": [
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1444011577830,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 10082800,
//	              "msecAfter": 1444021660630,
//	              "component": "Service",
//	              "optId": "AD24",
//	              "label": "foo",
//	              "parentOptId": "4F3D"
//	            },
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1444001495030,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 10082800,
//	              "msecAfter": 1444011577830,
//	              "component": "Service",
//	              "optId": "AE5F",
//	              "label": "bar",
//	              "parentOptId": "4F3D"
//	            },
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1443996453630,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 30248400,
//	              "msecAfter": 1444026702030,
//	              "component": "Service",
//	              "optId": "4F3D",
//	              "label": "Http servlet",
//	              "parentOptId": "23CF"
//	            },
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1443991412230,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 40331200,
//	              "msecAfter": 1444031743430,
//	              "component": "Proxy",
//	              "optId": "23CF",
//	              "label": "Service handler",
//	              "parentOptId": "DBF5"
//	            },
//	            {
//	              "taskId": "TID-ab1234-x",
//	              "msecBefore": 1443986370830,
//	              "host": "ukdb807735-3.local",
//	              "msecElapsed": 50414000,
//	              "msecAfter": 1444036784830,
//	              "component": "Proxy",
//	              "optId": "DBF5",
//	              "label": "Client handler",
//	              "parentOptId": "AAAA"
//	            }
//	          ]
//	        }
//	      }
//	    ],
//	    "records": 3,
//	    "offset": 0
//	  }
//	}
