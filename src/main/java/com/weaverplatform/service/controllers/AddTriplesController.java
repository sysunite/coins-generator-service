package com.weaverplatform.service.controllers;


import com.google.gson.Gson;
import com.weaverplatform.service.payloads.AddTriplesRequest;
import com.weaverplatform.service.payloads.JobReport;
import com.weaverplatform.service.util.ZipWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

public class AddTriplesController {

  private static Logger logger = LoggerFactory.getLogger(AddTriplesController.class);

  public static Gson gson = new Gson();

  public static Route add = (Request request, Response response) -> {

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      return new JobReport(false, "Please provide zipKey").toString(response);
    }

    AddTriplesRequest config;
    try {
      config = AddTriplesRequest.fromMultipart(request);
    } catch(Exception e) {
      return new JobReport(false, "Problem parsing config json in multi-part").toString(response);
    }

    if(!"turtle".equals(config.getRdfFormat().toLowerCase()) &&
    !"rdf/xml".equals(config.getRdfFormat().toLowerCase())) {
      return new JobReport(false, "Please set rdfFormat to either 'turtle' or 'rdf/xml'").toString(response);
    }

    logger.info("Starting thread to add triples to container "+zipKey);
    JobReport job = JobController.addJob();
    new Thread() {
      public void run() {
        ZipWriter.addRdfToZip(zipKey, config, job);
      }
    }.start();
    return job.toString(response);
  };

}
