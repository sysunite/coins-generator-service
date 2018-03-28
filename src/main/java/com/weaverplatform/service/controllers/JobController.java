package com.weaverplatform.service.controllers;


import com.weaverplatform.service.payloads.JobReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Map;

public class JobController {

  static Logger logger = LoggerFactory.getLogger(JobController.class);

  private static Map<String, JobReport> jobMap = new HashMap<>();

  public static JobReport addJob() {
    JobReport job = new JobReport();
    jobMap.put(job.getJobId(), job);
    return job;
  }

  public static Route getJob = (Request request, Response response) -> {

    String jobId = request.queryParamOrDefault("jobId", null);
    if(jobId == null || !jobMap.containsKey(jobId)) {
      return new JobReport(false, "JobId was not found").toString(response);
    }

    JobReport job = jobMap.get(jobId);
    return job.toString(response);
  };

}
