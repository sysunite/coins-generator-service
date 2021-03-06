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

/**
 * @author bastbijl, Sysunite 2018
 */
public class FileFromMultipartRequest implements AddFileRequest {

  @Expose(serialize = false)
  private InputStream file;

  @Expose
  private String path;


  public void setFile(InputStream file) {
    this.file = file;
  }

  public InputStream getFile() {
    return file;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public static FileFromMultipartRequest from(Request request) throws IOException, ServletException {
    MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp/multipart");
    request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
    Part file = request.raw().getPart("file");
    Part config = request.raw().getPart("config");

    Reader reader = new InputStreamReader(config.getInputStream(), "UTF-8");
    FileFromMultipartRequest result  = new Gson().fromJson(reader, FileFromMultipartRequest.class);
    result.setFile(file.getInputStream());
    return result;
  }
}
