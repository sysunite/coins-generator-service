package com.weaverplatform.service.controllers;


import com.google.gson.Gson;
import com.weaverplatform.service.payloads.AddFileRequest;
import com.weaverplatform.service.payloads.FileFromMultipartRequest;
import com.weaverplatform.service.payloads.JobReport;
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
    return new JobReport(true).toString(response);
  };

  public static Route addFile = (Request request, Response response) -> {

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      return new JobReport(false, "Please provide zipKey").toString(response);
    }

    AddFileRequest config;
    try {
      config = FileFromMultipartRequest.from(request);
    } catch(Exception e) {
      return new JobReport(false, "Problem parsing config json in multi-part").toString(response);
    }

    JobReport job = JobController.addJob();
    new Thread() {
      public void run() {
        try {
          ZipWriter.addToZip(zipKey, config);
          job.setSuccess(true);
        } catch (IOException e) {
          job.setMessage(e.getMessage());
          job.setSuccess(false);
        }
      }
    }.start();
    return job.toString(response);
  };

  public static Route download = (Request request, Response response) -> {

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      return new JobReport(false, "Please provide zipKey").toString(response);
    }

    // Read the resulting file
    HttpServletResponse raw = response.raw();
    try (OutputStream stream = raw.getOutputStream()) {
      ZipWriter.streamDownload(zipKey, stream);
    } catch(Exception e) {
      return new JobReport(false, ""+e.getMessage()+"").toString(response);
    }

    response.raw().setContentType("application/octet-stream");
    response.raw().setHeader("Content-Disposition","container; filename=container.ccr");
    return response.raw();
  };

  public static Route wipe = (Request request, Response response) -> {

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      return new JobReport(false, "Please provide zipKey").toString(response);
    }

    try {
      ZipWriter.wipe(zipKey);
    } catch(Exception e) {
      return new JobReport(false, ""+e.getMessage()+"").toString(response);
    }

    return new JobReport(true).toString(response);
  };

}
