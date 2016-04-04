package com.apm4all.tracy.backend;

import com.apm4all.tracy.apimodel.TaskAnalysis;
import com.apm4all.tracy.simulations.TaskAnalysisFake;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Headers;
import org.apache.camel.ProducerTemplate;

import java.io.IOException;
import java.util.Map;

import static com.apm4all.tracy.apimodel.Headers.*;

public class EsTaskAnalysis {
    private ProducerTemplate template;
    private EsTaskConfig esTaskConfig;

    public void setTemplate(ProducerTemplate template) {
        this.template = template;
    }

    public void setEsTaskConfig(EsTaskConfig esTaskConfig)	{
        this.esTaskConfig = esTaskConfig;
    }

    public TaskAnalysis getTaskAnalysis(
            Exchange exchange,
            @Header(APPLICATION) String application,
            @Header(TASK) String task,
            @Header(EARLIEST) String earliest,
            @Header(LATEST) String latest,
            @Header(FILTER) String filter,
            @Header(SORT) String sort,
            @Headers Map<String, Object> headers) throws IOException {
        // TODO: Define return type and input params
        // TODO: get list of (20) taskIds for search criteria (see EsQueryProcessor getTaskConfigFromEs)
        // TODO: Get Tracy events for each taskId
        // TODO: Fill in TaskAnalysis

        return new TaskAnalysisFake(application, task, Long.parseLong(earliest), Long.parseLong(latest), filter, sort, 20, 0);
    }
}
