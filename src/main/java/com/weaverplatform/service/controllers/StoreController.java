package com.weaverplatform.service.controllers;


import com.google.gson.Gson;
import com.weaverplatform.sdk.Weaver;
import com.weaverplatform.service.payloads.FileInStoreRequest;
import com.weaverplatform.service.payloads.JobReport;
import com.weaverplatform.service.util.Props;
import com.weaverplatform.service.util.ZipWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;

public class StoreController {

  static Logger logger = LoggerFactory.getLogger(StoreController.class);


  public static final String WEAVER_URI = Props.get("WEAVER_URI", "weaver.uri");
  public static final String USER = Props.get("SERVICE_USER","service.user");
  public static final String PASSWORD = Props.get("SERVICE_PASSWORD", "service.password");

  public static Gson gson = new Gson();

  public static Weaver getWeaver(String project) {
    Weaver instance = new Weaver();
    instance.setUri(WEAVER_URI);
    instance.setUsername(USER);
    instance.setPassword(PASSWORD);
    instance.login();
    instance.setProject(project);
    return instance;
  }

  public static Weaver getWeaver(String project, String autToken) {
    Weaver instance = new Weaver();
    instance.setUri(WEAVER_URI);
    instance.setAuthToken(autToken);
    instance.setProject(project);
    return instance;
  }

  public static Route fileFromStore = (Request request, Response response) -> {

    String project = request.queryParamOrDefault("project", null);
    if(project == null) {
      return new JobReport(false, "Please provide project").toString(response);
    }

    String authToken = request.queryParamOrDefault("user", null);
    if(authToken == null) {
      return new JobReport(false, "Please provide authToken").toString(response);
    }

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      return new JobReport(false, "Please provide zipKey").toString(response);
    }

    String path = request.queryParamOrDefault("path", null);
    if(path == null) {
      return new JobReport(false, "Please provide path").toString(response);
    }

    String fileId = request.queryParamOrDefault("fileId", null);
    if(fileId == null) {
      return new JobReport(false, "Please provide fileId").toString(response);
    }

    FileInStoreRequest config = new FileInStoreRequest();
    config.setPath(path);
    config.setFileId(fileId);


    Weaver weaver;
    try {
      weaver = getWeaver(project, authToken);
    } catch(RuntimeException e) {
      return new JobReport(false, "Connecting to weaver-server failed with this message: "+e.getMessage().replace("\"", "\\\"")+"").toString(response);
    }


    logger.info("Starting thread to stream a file from storage with container "+zipKey);
    JobReport job = JobController.addJob();
    new Thread() {
      public void run() {
        try {
          job.setProgress(0);
          job.setScale(2);
          config.setFile(weaver.downloadFile(config.getFileId()));
          job.setProgress(1);
          ZipWriter.addToZip(zipKey, config);
          job.setProgress(2);
          job.setSuccess(true);
        } catch (IOException e) {
          job.setSuccess(false);
          job.setMessage(e.getMessage());
        }
      }
    }.start();
    return job.toString(response);
  };

  public static Route fileToStore = (Request request, Response response) -> {

    String project = request.queryParamOrDefault("project", null);
    if(project == null) {
      return new JobReport(false, "Please provide project").toString(response);
    }

    String authToken = request.queryParamOrDefault("user", null);
    if(authToken == null) {
      return new JobReport(false, "Please provide authToken").toString(response);
    }

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      return new JobReport(false, "Please provide zipKey").toString(response);
    }

    String path = request.queryParamOrDefault("path", null);
    if(path == null) {
      return new JobReport(false, "Please provide path").toString(response);
    }

    Weaver weaver;
    try {
      weaver = getWeaver(project, authToken);
    } catch(RuntimeException e) {
      return new JobReport(false, "Connecting to weaver-server failed with this message: "+e.getMessage().replace("\"", "\\\"")+"").toString(response);
    }

    logger.info("Starting thread to stream a file to storage from container "+zipKey);
    JobReport job = JobController.addJob();
    new Thread() {
      public void run() {
        ZipWriter.streamFromZip(zipKey, path, weaver, job);
      }
    }.start();
    return job.toString(response);
  };

  public static Route containerToStore = (Request request, Response response) -> {

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      return new JobReport(false, "Please provide zipKey").toString(response);
    }

    String project = request.queryParamOrDefault("project", null);
    if(zipKey == null) {
      return new JobReport(false, "Please provide project").toString(response);
    }

    String authToken = request.queryParamOrDefault("user", null);
    if(authToken == null) {
      return new JobReport(false, "Please provide authToken").toString(response);
    }

    Weaver weaver;
    try {
      weaver = getWeaver(project, authToken);
    } catch(RuntimeException e) {
      return new JobReport(false, "Connecting to weaver-server failed with this message: "+e.getMessage().replace("\"", "\\\"")+"").toString(response);
    }

    logger.info("Starting thread to stream a file to storage with container "+zipKey);
    JobReport job = JobController.addJob();
    new Thread() {
      public void run() {
        ZipWriter.streamContainerDownload(zipKey, weaver, job);
      }
    }.start();
    return job.toString(response);
  };

}
