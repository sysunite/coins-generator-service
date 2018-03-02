package com.weaverplatform.service.controllers;


import com.weaverplatform.service.payloads.AddTriplesRequest;
import com.weaverplatform.service.util.ZipWriter;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;

public class RdfController {

  public static Route addXml = (Request request, Response response) -> {

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Please provide zipKey\"}";
    }

    AddTriplesRequest config;
    try {
      config = AddTriplesRequest.from(request);
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Problem parsing config json in multi-part\"}";
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

    response.status(200);
    response.type("application/json");
    return "{\"success\":true}";
  };

  public static Route addTtl = (Request request, Response response) -> {

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Please provide zipKey\"}";
    }

    AddTriplesRequest config;
    try {
      config = AddTriplesRequest.from(request);
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      return "{\"success\":false,\"message\":\"Problem parsing config json in multi-part\"}";
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
      return "{\"success\":false,\"message\":\""+e.getMessage()+"\"}";
    }

    response.status(200);
    response.type("application/json");
    return "{\"success\":true}";
  };

}
