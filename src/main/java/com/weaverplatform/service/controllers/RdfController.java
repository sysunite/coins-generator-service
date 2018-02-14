package com.weaverplatform.service.controllers;


import spark.Request;
import spark.Response;
import spark.Route;

import javax.servlet.ServletInputStream;

public class RdfController {

  /**
   * Route not found.
   */
  public static Route writeRdf = (Request req, Response res) -> {
    ServletInputStream input = req.raw().getInputStream();
    return null;
  };





}
