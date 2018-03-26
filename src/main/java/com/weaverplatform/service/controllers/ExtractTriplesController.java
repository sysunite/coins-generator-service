package com.weaverplatform.service.controllers;


import com.google.gson.Gson;
import com.weaverplatform.service.payloads.ExtractTriplesRequest;
import com.weaverplatform.service.payloads.Success;
import com.weaverplatform.service.util.ZipExtracter;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;

public class ExtractTriplesController {

  public static Gson gson = new Gson();

  public static Route addXml = (Request request, Response response) -> {

    ExtractTriplesRequest config;
    try {
      config = ExtractTriplesRequest.fromMultipart(request);
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Problem parsing config json in multi-part"));
    }

    response.status(200);
    response.type("application/json");
    try {
      ZipExtracter.writeOperationsXml(response.raw().getOutputStream(), config);

    } catch(IOException e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Could not extract write-ops"));
    } catch(RuntimeException e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, ""+e.getMessage()+""));
    }

    return response.raw();
  };

  public static Route addTtl = (Request request, Response response) -> {

    ExtractTriplesRequest config;
    try {
      config = ExtractTriplesRequest.fromMultipart(request);
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Problem parsing config json in multi-part"));
    }

    response.status(200);
    response.type("application/json");
    try {
      ZipExtracter.writeOperationsTtl(response.raw().getOutputStream(), config);

    } catch(IOException e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Could not extract write-ops"));
    } catch(RuntimeException e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, ""+e.getMessage()+""));
    }

    return response.raw();
  };

}
