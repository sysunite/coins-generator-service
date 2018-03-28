package com.weaverplatform.service.controllers;


import com.google.gson.Gson;
import com.weaverplatform.sdk.Weaver;
import com.weaverplatform.service.payloads.AddTriplesRequest;
import com.weaverplatform.service.payloads.JobReport;
import com.weaverplatform.service.util.DownloadedPart;
import com.weaverplatform.service.util.Props;
import com.weaverplatform.service.util.ZipWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.weaverplatform.service.controllers.StoreController.getWeaver;

public class SnapshotRdfController {

  static Logger logger = LoggerFactory.getLogger(SnapshotRdfController.class);

  public static Gson gson = new Gson();

  final static Boolean AUTO_CLEAN = Props.getBoolean("AUTO_CLEAN", "service.autoclean");

  public static Route add = (Request request, Response response) -> {

    String project = request.queryParamOrDefault("project", null);
    if(project == null) {
      return new JobReport(false, "Please provide project").toString(response);
    }
    logger.info("Using project '"+project+"'");

    String authToken = request.queryParamOrDefault("user", null);
    if(authToken == null) {
      return new JobReport(false, "Please provide authToken").toString(response);
    }

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      return new JobReport(false, "Please provide zipKey").toString(response);
    }

    List<String> graphs = new ArrayList<>();
    for(String graph : request.queryParamsValues("graphs")) {
      if(graph == null || graph.isEmpty() || "null".equals(graph) || "undefined".equals(graph)) {
        graphs.add(null); // for the default graph
      } else {
        graphs.add(graph);
      }
    }
    if(graphs == null || graphs.size() < 1) {
      return new JobReport(false, "Please provide at lease one graph name").toString(response);
    }

    Weaver weaver;
    try {
      weaver = getWeaver(project, authToken);
    } catch(RuntimeException e) {
      return new JobReport(false, "Connecting to weaver-server failed with this message: "+e.getMessage().replace("\"", "\\\"")+"").toString(response);
    }

    AddTriplesRequest config;
    try {
      config = AddTriplesRequest.fromBody(request);
      if(config == null) {
        throw new RuntimeException();
      }
    } catch(Exception e) {
      return new JobReport(false, "Problem parsing config json in body").toString(response);
    }

    try {
      for(String graph : graphs) {
        if(graph == null) {
          logger.info("Graph is null");
        } else {
          logger.info("Graph is '"+graph+"'");
        }
        DownloadedPart part = new DownloadedPart();
        InputStream zippedStream = weaver.getSnapshotGraph(graph, true);
        part.writeZippedStream(zippedStream);
        config.addPayload(part);
      }
    } catch(Exception e) {
      return new JobReport(false, "Problem retrieving write operations: "+e.getMessage()+"").toString(response);
    }

    if(!"turtle".equals(config.getRdfFormat().toLowerCase()) &&
       !"rdf/xml".equals(config.getRdfFormat().toLowerCase())) {
      return new JobReport(false, "Please set rdfFormat to either 'turtle' or 'rdf/xml'").toString(response);
    }

    JobReport job = JobController.addJob();
    new Thread() {
      public void run() {
        ZipWriter.addRdfToZip(zipKey, config, job);
        if(AUTO_CLEAN) {
          config.cleanUp();
        }
      }
    }.start();

    return job.toString(response);
  };

}
