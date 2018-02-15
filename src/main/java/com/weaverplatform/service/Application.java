package com.weaverplatform.service;

import com.weaverplatform.service.controllers.ApplicationController;
import com.weaverplatform.service.controllers.ContainerController;
import com.weaverplatform.service.util.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import static spark.Spark.*;

/**
 * The main Application class in which all processes are bootstrapped and
 * initialized. Default properties are loaded, Spark routing controllers are
 * initialised and mapped to their respective routes. Spark itself gets setup
 * and started.
 *
 * @author alex
 *
 */
public class Application {

  static Logger logger = LoggerFactory.getLogger(Application.class);

  public Application() {
    // Read properties
    final String NAME     = Props.get("application.name");
    final String VERSION  = Props.get("application.version");
    final int PORT        = Props.getInt("PORT", "application.port");

    staticFileLocation("/public");

    // Port for Spark to listen on
    port(PORT);

    // Setup thread pool
    threadPool(100, 5, 30000);

    // Route registration and mapping
    get("/",                   ApplicationController.about);

    post("/:zipKey/addRdf",            ContainerController.addRdf);
    post("/:zipKey/addTtl",            ContainerController.addTtl);
    post("/:zipKey/addFile",           ContainerController.addFile);

    get("/:zipKey/download",           ContainerController.download);

    get("/:zipKey/wipe",               ContainerController.wipe);

    get("/swagger", new Route() {
      @Override
      public Object handle(Request request, Response response) throws Exception {
        response.redirect("/swagger.yaml");
        return "no";
      }
    });

    // 404
    get("*",                   ApplicationController.notFound);

    // Wait for server initialization
    awaitInitialization();

    // Catch all other exceptions
    exception(Exception.class, (e, request, response) -> {
      logger.error("Server Error", e);
      response.status(503);
      response.body("503 - Server Error");
    });

    // Running
    logger.info("Running " + NAME + " " + VERSION + " on port " + PORT);
  }

  public static void main(String[] args) {
    new Application();
  }
}
