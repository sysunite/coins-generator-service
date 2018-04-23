package com.weaverplatform.service.controllers;


import com.weaverplatform.sdk.Weaver;
import com.weaverplatform.service.payloads.ExtractTriplesRequest;
import com.weaverplatform.service.payloads.JobReport;
import com.weaverplatform.service.util.towriteops.WriteOperationsExtractor;
import com.weaverplatform.service.util.towriteops.WriteOperationsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import static com.weaverplatform.service.controllers.StoreController.getWeaver;

public class ExtractTriplesController {

  private static Logger logger = LoggerFactory.getLogger(ExtractTriplesController.class);

  public static Route extract = (Request request, Response response) -> {

    String project = request.queryParamOrDefault("project", null);
    if(project == null) {
      return new JobReport(false, "Please provide project").toString(response);
    }

    String authToken = request.queryParamOrDefault("user", null);
    if(authToken == null) {
      return new JobReport(false, "Please provide authToken").toString(response);
    }

    String fileId = request.queryParamOrDefault("fileId", null);
    if(fileId == null) {
      return new JobReport(false, "Please provide fileId").toString(response);
    }

    ExtractTriplesRequest config;
    try {
      config = ExtractTriplesRequest.fromBody(request);
      config.setFileId(fileId);
    } catch(Exception e) {
      return new JobReport(false, "Problem parsing config json in body: "+request.body()).toString(response);
    }

    Weaver weaver;
    try {
      weaver = getWeaver(project, authToken);
    } catch(RuntimeException e) {
      return new JobReport(false, "Connecting to weaver-server failed with this message: "+e.getMessage().replace("\"", "\\\"")+"");
    }

    logger.info("Starting thread to import a container");
    JobReport job = JobController.addJob();
    new Thread() {
      public void run() {
        WriteOperationsModel model = WriteOperationsExtractor.loadModel(config, weaver, job);
        WriteOperationsExtractor.writeOperationsToStore(model, weaver, job);
      }
    }.start();
    return job.toString(response);
  };

  public static Route extractRdfFromFile = (Request request, Response response) -> {

    ExtractTriplesRequest config;
    try {
      config = ExtractTriplesRequest.fromMultipart(request);

    } catch(Exception e) {
      return new JobReport(false, "Problem parsing config json in body: "+request.body()).toString(response);
    }
    JobReport job = JobController.addJob();

    WriteOperationsModel model = WriteOperationsExtractor.loadModel(config, job);
    WriteOperationsExtractor.writeOperations(model, response.raw().getOutputStream(), job);
    return null;
  };

}
