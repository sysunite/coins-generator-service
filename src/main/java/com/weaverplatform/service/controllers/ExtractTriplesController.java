package com.weaverplatform.service.controllers;


import com.google.gson.Gson;
import com.weaverplatform.sdk.Weaver;
import com.weaverplatform.service.payloads.ExtractTriplesRequest;
import com.weaverplatform.service.payloads.Success;
import com.weaverplatform.service.util.WriteOperationsExtractor;
import spark.Request;
import spark.Response;
import spark.Route;

import static com.weaverplatform.service.controllers.StoreController.getWeaver;

public class ExtractTriplesController {

  public static Gson gson = new Gson();

  public static Route addXml = (Request request, Response response) -> {

    String project = request.queryParamOrDefault("project", null);
    if(project == null) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Please provide project"));
    }

    String authToken = request.queryParamOrDefault("user", null);
    if(authToken == null) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Please provide authToken"));
    }

    ExtractTriplesRequest config;
    try {
      config = ExtractTriplesRequest.fromMultipart(request);
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Problem parsing config json in multi-part"));
    }

    Weaver weaver;
    try {
      weaver = getWeaver(project, authToken);
    } catch(RuntimeException e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Connecting to weaver-server failed with this message: "+e.getMessage().replace("\"", "\\\"")+""));
    }

    try {
      WriteOperationsExtractor.writeOperationsXml(config, weaver);

    } catch(RuntimeException e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, ""+e.getMessage()+""));
    }

    response.status(200);
    response.type("application/json");
    return "{\"success\":true}";
  };

  public static Route addTtl = (Request request, Response response) -> {

    String project = request.queryParamOrDefault("project", null);
    if(project == null) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Please provide project"));
    }

    String authToken = request.queryParamOrDefault("user", null);
    if(authToken == null) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Please provide authToken"));
    }

    ExtractTriplesRequest config;
    try {
      config = ExtractTriplesRequest.fromMultipart(request);
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Problem parsing config json in multi-part"));
    }

    Weaver weaver;
    try {
      weaver = getWeaver(project, authToken);
    } catch(RuntimeException e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Connecting to weaver-server failed with this message: "+e.getMessage().replace("\"", "\\\"")+""));
    }

    try {
      WriteOperationsExtractor.writeOperationsTtl(config, weaver);

    } catch(RuntimeException e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, ""+e.getMessage()+""));
    }

    response.status(200);
    response.type("application/json");
    return "{\"success\":true}";
  };

}
