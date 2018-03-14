package com.weaverplatform.service.payloads;

/**
 * @author bastbijl, Sysunite 2018
 */
public class Success {

  public boolean success;
  public String message;
  public String fileId;

  public Success(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public Success(boolean success, String message, String fileId) {
    this.success = success;
    this.message = message;
    this.fileId = fileId;
  }
}
