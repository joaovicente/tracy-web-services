package com.apm4all.tracy.backend;

import com.apm4all.tracy.apimodel.TaskAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RetrievedTaskAnalysis implements TaskAnalysis{

    private String application;
    private String task;
    private long earliest;
    private long latest;
    private String filter;
    private String sort;
    private int limit;
    private int offset;
    private HashMap<String, Object> tracyTasksPage;

    public RetrievedTaskAnalysis(
            String application, String task, long earliest, long latest,
            String filter, String sort, int limit, int offset)   {
        this.application = application;
        this.task = task;
        this.earliest = earliest;
        this.latest = latest;
        this.filter = filter;
        this.sort = sort;
        this.limit = limit;
        this.offset = offset;
    }

    @Override
    public String getApplication() {
        return this.application;
    }

    @Override
    public String getTask() {
        return this.task;
    }

    @Override
    public HashMap<String, Object> getTracyTasksPage() {
        return tracyTasksPage;
    }

    @Override
    public long getEarliest() {
        return this.earliest;
    }

    @Override
    public long getLatest() {
        return this.latest;
    }

    @Override
    public String getFilter() {
        return this.filter;
    }

    @Override
    public String getSort() {
        return this.sort;
    }

    public void setTracyTasks(List<String> taskIds, Map<String, List<Object>> tracyEventsMap) {
        HashMap<String,Object> tracyTasksPage = new HashMap<String,Object>();
        tracyTasksPage.put("limit", this.limit);
        tracyTasksPage.put("offset", this.offset);
        tracyTasksPage.put("records", taskIds.size());
        ArrayList<Object> tracyTasks = new ArrayList<Object>();

        for (String taskId : taskIds)   {
            Map<String,Object> tracyTask = new HashMap<String,Object>();
            Map<String,Object> tracyEventContainer = new HashMap<String,Object>();
            ArrayList<Object> tracyEvents = new ArrayList<Object>();
            for (Object tracyEvent : tracyEventsMap.get(taskId)) {
                tracyEvents.add(tracyEvent);
            }
            tracyEventContainer.put("tracyEvents", tracyEvents);
            tracyTask.put("tracyTask", tracyEventContainer);
            tracyTasks.add(tracyTask);
        }
        tracyTasksPage.put("tracyTasks", tracyTasks);
        this.tracyTasksPage = tracyTasksPage;

//        "tracyTasksPage" : {
//            "limit" : 20,
//            "tracyTasks" : [ {
//                "tracyTask" : {
//                    "tracyEvents" : [ {
//                        "taskId" : "TID-ab1234-x",
//                                "msecBefore" : 1443985201009,
//                                "host" : "46.7.188.254",
//                                "msecElapsed" : 26,
//                                "msecAfter" : 1443985201035,
//                                "component" : "Service",
//                                "optId" : "AD24",
//                                "label" : "foo",
//                                "parentOptId" : "4F3D"
//                    }, {
    }
}
