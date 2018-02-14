package com.weaverplatform.service.controllers;

import com.google.gson.Gson;
import com.weaverplatform.service.util.Props;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 *
 * @author alex
 *
 */
public class ApplicationController {

  /**
   * Displays some basic data about the server.
   */
  public static Route about = (Request req, Response res) -> {
    res.type("application/json");
    return new About().toJson();
  };

  /**
   * Route not found.
   */
  public static Route notFound = (Request req, Response res) -> {
    res.status(404);
    return "404 - Route not found";
  };

  static class About {
    private String name, version, source;

    public About() {
      this.name    = Props.get("application.name");
      this.version = Props.get("application.version");
    }

    public String toJson(){
      return new Gson().toJson(this);
    }
  }
}
