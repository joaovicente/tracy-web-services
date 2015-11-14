package com.apm4all.tracy.analysis.task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskAnalysis {
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

	public TaskAnalysis(String application, String task)	{
		// TODO: handle params: earliest, latest, filter, sort
		this.application = application;
		this.task = task;
		this.earliest = 1446415872559L;
		this.latest = 	1446425872559L;
		this.filter = "";
		this.sort = "";
		this.records = 18;
		this.offset = 0;
		this.limit = 20;
		this.tracyTasks = new ArrayList<Object>(200);
	}
	
	public String getApplication()	{
		return this.application;
	}
	
	public String getTask()	{
		return this.task;
	}
	
	public HashMap<String,Object> getTracyTasksPage()	{
		//TODO: Return structure line below
		HashMap<String,Object> tracyTasksPage = new HashMap<String,Object>();
		tracyTasksPage.put("application", this.application);
		tracyTasksPage.put("task", this.task);
		tracyTasksPage.put("offset", this.offset);
		tracyTasksPage.put("records", this.records);
		ArrayList<Object> tracyTasks = new ArrayList<Object>();
		tracyTasksPage.put("tracyTasks", tracyTasks);
	
		// Create mocked up tracyTasks
		for (int i=0 ; i<records ; i++)	{
			tracyTasks.add(generateTracyTask(3600000L*i));
		}
		return tracyTasksPage;
	}

	private ArrayList<Object> generateTracyTask(long timeOffset)	{
		ArrayList<Object> tracyTask = new ArrayList<Object>(20);
	    long rt = this.earliest;
	    // var offset = 10; // msecOffset
	    // var offset = 1010; // secOffset
	    // var offset = 61010; // minOffset
	    long offset = 3601000L; // hourOffset
	    String host = "ukdb807735-3.local";
	    tracyTask.add(createTracyEvent("TID-ab1234-x", "4F3D", "foo", "AD24", timeOffset+rt+offset*5, timeOffset+rt+offset*7, offset*2, host, "Service"));
	    tracyTask.add(createTracyEvent("TID-ab1234-x", "4F3D", "bar", "AE5F", timeOffset+rt+offset*3, timeOffset+rt+offset*5, offset*2, host, "Service"));
	    tracyTask.add(createTracyEvent("TID-ab1234-x", "23CF", "Http servlet", "4F3D", timeOffset+rt+offset*2, timeOffset+rt+offset*8, offset*6, host, "Service"));
	    tracyTask.add(createTracyEvent("TID-ab1234-x", "DBF5", "Service handler", "23CF", timeOffset+rt+offset, timeOffset+rt+offset*9, offset*8, host, "Proxy"));
	    tracyTask.add(createTracyEvent("TID-ab1234-x", "AAAA", "Client handler", "AD24", timeOffset+rt, timeOffset+rt+offset*10, offset*10, host, "Proxy"));
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

	public long getEarliest() {
		return earliest;
	}

	public long getLatest() {
		return latest;
	}

	public String getFilter() {
		return filter;
	}

	public String getSort() {
		return sort;
	}

	public int getOffset() {
		return offset;
	}

	public int getLimit() {
		return limit;
	}

	public int getRecords() {
		return records;
	}

	public ArrayList<Object> getTracyTasks() {
		return tracyTasks;
	}
}

//"tracyTasksPage": {
//    "offset": 0,
//    "limit": 2,
//    "records": 2,
//    "tracyTasks": [
//      {
//        "tracyTask": {
//          "tracyEvents": [
//            {
//              "taskId": "rrt017eudn-7245-675317-1",
//              "parentOptId": "246A",
//              "label": "transformEMF1ToJson",
//              "optId": "CD54",
//              "msecBefore": 1446163199518,
//              "msecAfter": 1446163199518,
//              "msecElapsed": 0,
//              "component": "matchplus-ws",
//              "host": "ip-10-241-0-39"
//            },
//            {
//              "taskId": "rrt017eudn-7245-675317-1",
//              "parentOptId": "AG50",
//              "label": "getCleanseMatch",
//              "optId": "246A",
//              "msecBefore": 1446163198129,
//              "msecAfter": 1446163199519,
//              "msecElapsed": 1390,
//              "subscriberType": "internal",
//              "companyName": "Sarada Prasana Jena",
//              "apigee.developer.app.name": "JenaS-Match-App-Prod",
//              "lookupType": "NA",
//              "reqParam": "MONSIEUR PIERRE CHALLET",
//              "ctryCd": "FR",
//              "component": "matchplus-ws",
//              "sourceIp": "37.252.225.18",
//              "apigee.developer.email": "jenas@dnb.com",
//              "host": "ip-10-241-0-39",
//              "subscriberNumber": "5008",
//              "engineName": "GF"
//            },
//            {
//              "taskId": "rrt017eudn-7245-675317-1",
//              "parentOptId": "246A",
//              "label": "transformJsonToEMF1",
//              "optId": "9FD5",
//              "msecBefore": 1446163198129,
//              "msecAfter": 1446163198129,
//              "msecElapsed": 0,
//              "component": "matchplus-ws",
//              "host": "ip-10-241-0-39"
//            },
//            {
//              "taskId": "rrt017eudn-7245-675317-1",
//              "parentOptId": "246A",
//              "label": "matchEngineGF",
//              "optId": "3278",
//              "msecBefore": 1446163198129,
//              "msecAfter": 1446163199518,
//              "msecElapsed": 1389,
//              "lookupType": "NA",
//              "component": "matchplus-ws",
//              "host": "ip-10-241-0-39",
//              "engineName": "GF"
//            }
//          ]
//        }
//      },
//      {
//        "tracyTask": {
//          "tracyEvents": [
//            {
//              "taskId": "rrt056wodn-25309-693227-1",
//              "parentOptId": "246A",
//              "label": "transformEMF1ToJson",
//              "optId": "CD54",
//              "msecBefore": 1446163199518,
//              "msecAfter": 1446163199518,
//              "msecElapsed": 0,
//              "component": "matchplus-ws",
//              "host": "ip-10-241-0-39"
//            },
//            {
//              "taskId": "rrt056wodn-25309-693227-1",
//              "parentOptId": "AG50",
//              "label": "getCleanseMatch",
//              "optId": "246A",
//              "msecBefore": 1446163198129,
//              "msecAfter": 1446163199519,
//              "msecElapsed": 1390,
//              "subscriberType": "internal",
//              "companyName": "Sarada Prasana Jena",
//              "apigee.developer.app.name": "JenaS-Match-App-Prod",
//              "lookupType": "NA",
//              "reqParam": "MONSIEUR PIERRE CHALLET",
//              "ctryCd": "FR",
//              "component": "matchplus-ws",
//              "sourceIp": "37.252.225.18",
//              "apigee.developer.email": "jenas@dnb.com",
//              "host": "ip-10-241-0-39",
//              "subscriberNumber": "5008",
//              "engineName": "GF"
//            },
//            {
//              "taskId": "rrt056wodn-25309-693227-1",
//              "parentOptId": "246A",
//              "label": "transformJsonToEMF1",
//              "optId": "9FD5",
//              "msecBefore": 1446163198129,
//              "msecAfter": 1446163198129,
//              "msecElapsed": 0,
//              "component": "matchplus-ws",
//              "host": "ip-10-241-0-39"
//            },
//            {
//              "taskId": "rrt056wodn-25309-693227-1",
//              "parentOptId": "246A",
//              "label": "matchEngineGF",
//              "optId": "3278",
//              "msecBefore": 1446163198129,
//              "msecAfter": 1446163199518,
//              "msecElapsed": 1389,
//              "lookupType": "NA",
//              "component": "matchplus-ws",
//              "host": "ip-10-241-0-39",
//              "engineName": "GF"
//            }
//          ]
//        }
//      }
//    ]
//  }
