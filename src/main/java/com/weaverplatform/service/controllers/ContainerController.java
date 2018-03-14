package com.weaverplatform.service.controllers;


import com.google.gson.Gson;
import com.weaverplatform.service.payloads.AddFileRequest;
import com.weaverplatform.service.payloads.FileFromMultipartRequest;
import com.weaverplatform.service.payloads.Success;
import com.weaverplatform.service.util.ZipWriter;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class ContainerController {

  public static Gson gson = new Gson();

  public static Route resetLocks = (Request request, Response response) -> {
    ZipWriter.resetBlockList();
    response.status(200);
    response.type("application/json");
    return "{\"success\":true}";
  };

  public static Route addFile = (Request request, Response response) -> {

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Please provide zipKey"));
    }

    AddFileRequest config;
    try {
      config = FileFromMultipartRequest.from(request);
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Problem parsing config json in multi-part"));
    }

    try {
      ZipWriter.addToZip(zipKey, config);

    } catch(IOException e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Could not write file to "+zipKey+""));
    } catch(RuntimeException e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "" + e.getMessage() + ""));
    }

    response.status(200);
    response.type("application/json");
    return "{\"success\":true}";
  };

  public static Route download = (Request request, Response response) -> {

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Please provide zipKey"));
    }

    // Read the resulting file
    HttpServletResponse raw = response.raw();
    try (OutputStream stream = raw.getOutputStream()) {
      ZipWriter.streamDownload(zipKey, stream);
    } catch(Exception e) {
      response.status(500);
      response.body();
      return gson.toJson(new Success(false, ""+e.getMessage()+""));
    }

    response.raw().setContentType("application/octet-stream");
    response.raw().setHeader("Content-Disposition","container; filename=container.ccr");
    return response.raw();
  };

  public static Route wipe = (Request request, Response response) -> {

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Please provide zipKey"));
    }

    try {
      ZipWriter.wipe(zipKey);
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, ""+e.getMessage()+""));
    }

    response.status(200);
    response.type("application/json");
    return "{\"success\":true}";
  };

}
