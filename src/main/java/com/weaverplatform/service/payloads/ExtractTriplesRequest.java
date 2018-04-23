package com.weaverplatform.service.payloads;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import spark.Request;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author bastbijl, Sysunite 2018
 */
public class ExtractTriplesRequest {

  @Expose
  private HashMap<String, String> prefixMap;

  @Expose
  private HashMap<String, String> graphMap;

  @Expose
  private String defaultGraph;

  @Expose
  private HashSet<String> dismissGraphs;

  @Expose
  private String rdfFormat;

  @Expose
  private String path;

  @Expose
  private String fileId;

  @Expose(serialize = false)
  private InputStream file;


  public void setPrefixMap(HashMap<String, String> prefixMap) {
    this.prefixMap = prefixMap;
  }

  public HashMap<String, String> getPrefixMap() {
    return prefixMap;
  }

  public void setGraphMap(HashMap<String, String> graphMap) {
    this.graphMap = graphMap;
  }

  public HashMap<String, String> getGraphMap() {
    return graphMap;
  }

  public void setDefaultGraph(String defaultGraph) {
    this.defaultGraph = defaultGraph;
  }

  public String getDefaultGraph() {
    return defaultGraph;
  }

  public void setDismissGraphs(HashSet<String> dismissGraphs) {
    this.dismissGraphs = dismissGraphs;
  }

  public HashSet<String> getDismissGraphs() {
    return dismissGraphs;
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

  public void setFile(InputStream file) {
    this.file = file;
  }

  public InputStream getFile() {
    return file;
  }

  public static ExtractTriplesRequest fromBody(Request request) throws IOException, ServletException {
    ExtractTriplesRequest result  = new Gson().fromJson(request.body(), ExtractTriplesRequest.class);
    return result;
  }

  public static ExtractTriplesRequest fromMultipart(Request request) throws IOException, ServletException {
    MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp/multipart");
    request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
    Part file = request.raw().getPart("file");
    Part config = request.raw().getPart("config");

    Reader reader = new InputStreamReader(config.getInputStream(), "UTF-8");
    ExtractTriplesRequest result = new Gson().fromJson(reader, ExtractTriplesRequest.class);
    result.setFile(file.getInputStream());
    return result;
  }
}
