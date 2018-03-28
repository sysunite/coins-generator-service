package com.weaverplatform.service.controllers;


import com.weaverplatform.sdk.Weaver;
import com.weaverplatform.service.payloads.ExtractTriplesRequest;
import com.weaverplatform.service.payloads.JobReport;
import com.weaverplatform.service.util.WriteOperationsExtractor;
import spark.Request;
import spark.Response;
import spark.Route;

import static com.weaverplatform.service.controllers.StoreController.getWeaver;

public class ExtractTriplesController {

  public static Route extract = (Request request, Response response) -> {

    String project = request.queryParamOrDefault("project", null);
    if(project == null) {
      return new JobReport(false, "Please provide project").toString(response);
    }

    String authToken = request.queryParamOrDefault("user", null);
    if(authToken == null) {
      return new JobReport(false, "Please provide authToken").toString(response);
    }

    ExtractTriplesRequest config;
    try {
      config = ExtractTriplesRequest.fromMultipart(request);
    } catch(Exception e) {
      return new JobReport(false, "Problem parsing config json in multi-part").toString(response);
    }

    Weaver weaver;
    try {
      weaver = getWeaver(project, authToken);
    } catch(RuntimeException e) {
      return new JobReport(false, "Connecting to weaver-server failed with this message: "+e.getMessage().replace("\"", "\\\"")+"");
    }

    JobReport job = JobController.addJob();
    new Thread() {
      public void run() {
        WriteOperationsExtractor.writeOperations(config, weaver, job);
      }
    }.start();
    return job.toString(response);
  };

}
