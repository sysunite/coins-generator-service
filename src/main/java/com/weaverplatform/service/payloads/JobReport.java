package com.weaverplatform.service.payloads;

import com.google.gson.Gson;
import com.weaverplatform.service.util.Cuid;
import spark.Response;

/**
 * @author bastbijl, Sysunite 2018
 */
public class JobReport {

  public static Gson gson = new Gson();

  public String jobId;
  public Boolean success;
  public String message;
  public String fileId;
  public int progress;
  public int scale;

  public JobReport() {
    this.jobId = Cuid.getRandomBlock();
  }

  public JobReport(boolean success) {
    this.success = success;
  }

  public JobReport(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public JobReport setSuccess(boolean success) {
    this.success = success;
    return this;
  }

  public JobReport setMessage(String message) {
    this.message = message;
    return this;
  }

  public JobReport setFileId(String fileId) {
    this.fileId = fileId;
    return this;
  }

  public JobReport setProgress(int progress) {
    this.progress = progress;
    return this;
  }

  public JobReport setScale(int scale) {
    this.scale = scale;
    return this;
  }

  public String getJobId() {
    return jobId;
  }

  public String toString(Response response) {
    if(success == null || success) {
      response.status(200);
    } else {
      response.status(500);
    }
    response.type("application/json");
    return gson.toJson(this);
  }
}
