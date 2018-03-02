package com.weaverplatform.service.payloads;

import com.google.gson.annotations.Expose;

import java.io.InputStream;

/**
 * @author bastbijl, Sysunite 2018
 */
public class FileFromStoreRequest implements AddFileRequest {

  @Expose(serialize = false)
  private InputStream file;

  @Expose
  private String path;

  @Expose
  private String fileId;


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

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public String getFileId() {
    return fileId;
  }
}
