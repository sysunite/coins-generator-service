package com.weaverplatform.service.controllers;


import com.weaverplatform.sdk.Weaver;
import com.weaverplatform.service.payloads.AddTriplesRequest;
import com.weaverplatform.service.util.DownloadedPart;
import com.weaverplatform.service.util.ZipWriter;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.weaverplatform.service.controllers.StoreController.getWeaver;

public class SnapshotRdfController {

  public static Route addXml = (Request request, Response response) -> {

    String project = request.queryParamOrDefault("project", null);
    if(project == null) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Please provide project\"}";
    }

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Please provide zipKey\"}";
    }

    List<String> graphs = new ArrayList<>();
    for(String graph : request.queryParamsValues("graphs")) {
      if(graph == null || "null".equals(graph) || "undefined".equals(graph)) {
        graphs.add(null); // for the default graph
      } else {
        graphs.add(graph);
      }
    }
    if(graphs == null || graphs.size() < 1) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Please provide at lease one graph name\"}";
    }

    Weaver weaver;
    try {
      weaver = getWeaver(project);
    } catch(RuntimeException e) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Connecting to weaver-server failed with this message: "+e.getMessage().replace("\"", "\\\"")+"\"}";
    }

    AddTriplesRequest config;
    try {
      config = AddTriplesRequest.fromBody(request);
      if(config == null) {
        throw new RuntimeException();
      }
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Problem parsing config json in body\"}";
    }

    try {
      System.out.println(graphs.size());
      for(String graph : graphs) {
        System.out.println("graph: "+graph);
        DownloadedPart part = new DownloadedPart();
        InputStream zippedStream = weaver.getSnapshotGraph(graph, true);
        part.writeZippedStream(zippedStream);
        config.addPayload(part);
      }
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      e.printStackTrace();
      return "{\"success\":false,\"message\":\"Problem retrieving write operations: "+e.getMessage()+"\"}";
    }

    try {
      ZipWriter.addXmlToZip(zipKey, config);

    } catch(IOException e) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Could not write the write-ops to the container\"}";
    } catch(RuntimeException e) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\""+e.getMessage()+"\"}";
    }

    config.cleanUp();

    response.status(200);
    response.type("application/json");
    return "{\"success\":true}";
  };

  public static Route addTtl = (Request request, Response response) -> {

    String project = request.queryParamOrDefault("project", null);
    if(project == null) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Please provide project\"}";
    }

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Please provide zipKey\"}";
    }

    List<String> graphs = new ArrayList<>();
    for(String graph : request.queryParamsValues("graphs")) {
      if(graph == null || "null".equals(graph) || "undefined".equals(graph)) {
        graphs.add(null); // for the default graph
      } else {
        graphs.add(graph);
      }
    }
    if(graphs == null || graphs.size() < 1) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Please provide at lease one graph name\"}";
    }

    Weaver weaver;
    try {
      weaver = getWeaver(project);
    } catch(RuntimeException e) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Connecting to weaver-server failed with this message: "+e.getMessage().replace("\"", "\\\"")+"\"}";
    }

    AddTriplesRequest config;
    try {
      config = AddTriplesRequest.fromBody(request);
      if(config == null) {
        throw new RuntimeException();
      }
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Problem parsing config json in body\"}";
    }

    try {
      System.out.println(graphs.size());
      for(String graph : graphs) {
        System.out.println("graph: "+graph);
        DownloadedPart part = new DownloadedPart();
        InputStream zippedStream = weaver.getSnapshotGraph(graph, true);
        part.writeZippedStream(zippedStream);
        config.addPayload(part);
      }
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      e.printStackTrace();
      return "{\"success\":false,\"message\":\"Problem retrieving write operations: "+e.getMessage()+"\"}";
    }

    try {
      ZipWriter.addTtlToZip(zipKey, config);

    } catch(IOException e) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Could not write the write-ops to the container\"}";
    } catch(RuntimeException e) {
      response.status(500);
      response.type("application/json");
      e.printStackTrace();
      return "{\"success\":false,\"message\":\""+e.getMessage()+"\"}";
    }

    config.cleanUp();

    response.status(200);
    response.type("application/json");
    return "{\"success\":true}";
  };

}
