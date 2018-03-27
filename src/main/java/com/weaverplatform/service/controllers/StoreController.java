package com.weaverplatform.service.controllers;


import com.google.gson.Gson;
import com.weaverplatform.sdk.Weaver;
import com.weaverplatform.service.payloads.FileFromStoreRequest;
import com.weaverplatform.service.payloads.Success;
import com.weaverplatform.service.util.Props;
import com.weaverplatform.service.util.ZipWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

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

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Please provide zipKey"));
    }

    String path = request.queryParamOrDefault("path", null);
    if(path == null) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Please provide path"));
    }

    String fileId = request.queryParamOrDefault("fileId", null);
    if(fileId == null) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Please provide fileId"));
    }

    FileFromStoreRequest config = new FileFromStoreRequest();
    config.setPath(path);
    config.setFileId(fileId);


    Weaver weaver;
    try {
      weaver = getWeaver(project, authToken);
    } catch(RuntimeException e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Connecting to weaver-server failed with this message: "+e.getMessage().replace("\"", "\\\"")+""));
    }

    try {
      config.setFile(weaver.downloadFile(config.getFileId()));
      ZipWriter.addToZip(zipKey, config);
    } catch(Exception e) {
      response.status(500);
      response.body(e.getMessage());
    }

    response.status(200);
    response.type("application/json");
    return "{\"success\":true}";
  };

  public static Route containerToStore = (Request request, Response response) -> {

    String zipKey = request.queryParamOrDefault("zipKey", null);
    if(zipKey == null) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Please provide zipKey"));
    }

    String project = request.queryParamOrDefault("project", null);
    if(zipKey == null) {
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

    Weaver weaver;
    try {
      weaver = getWeaver(project, authToken);
    } catch(RuntimeException e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, "Connecting to weaver-server failed with this message: "+e.getMessage().replace("\"", "\\\"")+""));
    }

    try {
      String fileId = ZipWriter.streamDownload(zipKey, weaver).getId();

      response.status(200);
      response.type("application/json");
      return gson.toJson(new Success(true, "", fileId));
    } catch(Exception e) {
      response.status(500);
      response.type("application/json");
      return gson.toJson(new Success(false, ""+e.getMessage()+""));
    }
  };

}
