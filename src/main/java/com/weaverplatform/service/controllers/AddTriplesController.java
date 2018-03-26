package com.weaverplatform.service.controllers;


import com.google.gson.Gson;
import com.weaverplatform.service.payloads.AddTriplesRequest;
import com.weaverplatform.service.payloads.Success;
import com.weaverplatform.service.util.ZipWriter;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;

public class AddTriplesController {

  public static Gson gson = new Gson();

  public static Route addXml = (Request request, Response response) -> {

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Please provide zipKey"));
    }

    AddTriplesRequest config;
    try {
      config = AddTriplesRequest.fromMultipart(request);
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Problem parsing config json in multi-part"));
    }

    try {
      ZipWriter.addXmlToZip(zipKey, config);

    } catch(IOException e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Could not write the write-ops to the container"));
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

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Please provide zipKey"));
    }

    AddTriplesRequest config;
    try {
      config = AddTriplesRequest.fromMultipart(request);
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Problem parsing config json in multi-part"));
    }

    try {
      ZipWriter.addTtlToZip(zipKey, config);

    } catch(IOException e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Could not write the write-ops to the container"));
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
