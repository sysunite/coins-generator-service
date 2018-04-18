package com.weaverplatform.service.controllers;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.weaverplatform.sdk.Weaver;
import com.weaverplatform.service.payloads.AddFileRequest;
import com.weaverplatform.service.payloads.FileFromMultipartRequest;
import com.weaverplatform.service.payloads.JobReport;
import com.weaverplatform.service.util.ZipWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.weaverplatform.service.controllers.StoreController.getWeaver;

public class ContainerController {

  private static Logger logger = LoggerFactory.getLogger(ContainerController.class);

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

    logger.info("Starting thread to add file to container "+zipKey);
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



  public static Route listContent = (Request request, Response response) -> {

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


    Weaver weaver;
    try {
      weaver = getWeaver(project, authToken);
    } catch(RuntimeException e) {
      return new JobReport(false, "Connecting to weaver-server failed with this message: "+e.getMessage().replace("\"", "\\\"")+"");
    }

    JsonArray outline = new JsonArray();
    InputStream containerFileStream;
    try {
      containerFileStream = weaver.downloadFile(fileId);

      ZipInputStream zipStream = new ZipInputStream(containerFileStream);
      while(zipStream.available() > 0) {
        ZipEntry entry = zipStream.getNextEntry();
        if(entry != null) {
          outline.add(entry.getName());
        }
      }

    } catch (IOException e) {
      return new JobReport(false, "Could not read zip file: "+e.getMessage().replace("\"", "\\\"")+"");
    }
    response.type("application/json");
    return outline.toString();
  };

}
