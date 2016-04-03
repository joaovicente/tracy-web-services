package com.apm4all.tracy.backend;

import com.apm4all.tracy.apimodel.TaskAnalysis;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Headers;

import java.io.IOException;
import java.util.Map;

import static com.apm4all.tracy.apimodel.Headers.*;

public class EsTaskAnalysis {
    public TaskAnalysis getTaskAnalysis()	{
            Exchange exchange,
            @Header(APPLICATION) String application,
            @Header(TASK) String task,
            @Header(EARLIEST) String earliest,
            @Header(LATEST) String latest,
            @Header(SNAP) String snap,
            @Headers Map<String, Object> headers) throws IOException {
            // TODO: Define return type and input params
            // TODO: get list of (20) taskIds for search criteria (see EsQueryProcessor getTaskConfigFromEs)
            // TODO: Get Tracy events for each taskId
            // TODO: Fill in TaskAnalysis
        }
}
