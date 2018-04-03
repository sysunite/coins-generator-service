package com.weaverplatform.service.payloads;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import spark.Request;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author bastbijl, Sysunite 2018
 */
public class ExtractTriplesRequest {

  @Expose
  private HashMap<String, String> prefixMap;

  @Expose
  private String toGraph;

  @Expose
  private String rdfFormat;

  @Expose
  private String path;

  @Expose
  private String fileId;


  public void setPrefixMap(HashMap<String, String> prefixMap) {
    this.prefixMap = prefixMap;
  }

  public HashMap<String, String> getPrefixMap() {
    return prefixMap;
  }

  public void setToGraph(String toGraph) {
    this.toGraph = toGraph;
  }

  public String getToGraph() {
    return toGraph;
  }

  public void setRdfFormat(String rdfFormat) {
    this.rdfFormat = rdfFormat;
  }

  public String getRdfFormat() {
    return rdfFormat;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public String getFileId() {
    return fileId;
  }

  public static ExtractTriplesRequest fromBody(Request request) throws IOException, ServletException {
    ExtractTriplesRequest result  = new Gson().fromJson(request.body(), ExtractTriplesRequest.class);
    return result;
  }
}
