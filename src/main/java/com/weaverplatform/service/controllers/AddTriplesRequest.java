package com.weaverplatform.service.controllers;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import spark.Request;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;

/**
 * @author bastbijl, Sysunite 2018
 */
public class AddTriplesRequest {

  @Expose(serialize = false)
  private Part payload;

  @Expose
  private HashMap<String, String> prefixMap;

  @Expose
  private String defaultPrefix;

  @Expose
  private String mainContext;

  @Expose
  private String path;


  public void setPayload(Part payload) {
    this.payload = payload;
  }

  public Part getPayload() {
    return payload;
  }

  public void setPrefixMap(HashMap<String, String> prefixMap) {
    this.prefixMap = prefixMap;
  }

  public HashMap<String, String> getPrefixMap() {
    return prefixMap;
  }

  public void setDefaultPrefix(String defaultPrefix) {
    this.defaultPrefix = defaultPrefix;
  }

  public String getDefaultPrefix() {
    return defaultPrefix;
  }

  public void setMainContext(String mainContext) {
    this.mainContext = mainContext;
  }

  public String getMainContext() {
    return mainContext;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public static AddTriplesRequest from(Request request) throws IOException, ServletException {
    MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp/multipart");
    request.raw().setAttribute("org.eclipse.multipartConfig", multipartConfigElement);
    Part payload = request.raw().getPart("payload");
    Part config = request.raw().getPart("config");

    Reader reader = new InputStreamReader(config.getInputStream(), "UTF-8");
    AddTriplesRequest result  = new Gson().fromJson(reader, AddTriplesRequest.class);
    result.setPayload(payload);
    return result;
  }
}
