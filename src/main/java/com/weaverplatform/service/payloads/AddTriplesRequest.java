package com.weaverplatform.service.payloads;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import spark.Request;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author bastbijl, Sysunite 2018
 */
public class AddTriplesRequest {

  @Expose(serialize = false)
  private List<Part> payloads = null;

  @Expose
  private HashMap<String, String> prefixMap;

  @Expose
  private String defaultPrefix;

  @Expose
  private String mainContext;

  @Expose
  private String path;

  @Expose
  private Boolean reify;


  public void addPayload(Part payload) {
    if(this.payloads == null) {
      this.payloads = new ArrayList<>();
    }
    this.payloads.add(payload);
  }

  public List<Part> getPayloads() {
    return payloads;
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

  public void setReify(Boolean reify) {
    this.reify = reify;
  }

  public Boolean getReify() {
    return reify;
  }

  public boolean reify() {
    return reify != null && reify;
  }

  public static AddTriplesRequest fromMultipart(Request request) throws IOException, ServletException {
    MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp/multipart");
    request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
    Part payload = request.raw().getPart("payload");
    Part config = request.raw().getPart("config");

    Reader reader = new InputStreamReader(config.getInputStream(), "UTF-8");
    AddTriplesRequest result  = new Gson().fromJson(reader, AddTriplesRequest.class);
    result.addPayload(payload);
    return result;
  }

  public static AddTriplesRequest fromBody(Request request) throws IOException, ServletException {
    AddTriplesRequest result  = new Gson().fromJson(request.body(), AddTriplesRequest.class);
    return result;
  }

  public void cleanUp() {
    if(this.payloads == null) {
      return;
    }
    for(Part part : payloads) {
      try {
        part.delete();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
