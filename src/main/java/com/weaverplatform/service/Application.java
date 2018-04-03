package com.weaverplatform.service;

import com.weaverplatform.service.controllers.*;
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
    get("/",                    ApplicationController.about);

    get("/status",              JobController.getJob);

    get("/resetLocks",          ContainerController.resetLocks);
    get("/wipe",                ContainerController.wipe);
    get("/download",            ContainerController.download);

    get("/containerToStore",    StoreController.containerToStore);  // Spins off and returns job
    get("/fileFromStore",       StoreController.fileFromStore);     // Spins off and returns job

    post("/addFile",            ContainerController.addFile);       // Spins off and returns job

    post("/addRdf",             AddTriplesController.add);          // Spins off and returns job

    post("/addRdfFromSnapshot", SnapshotRdfController.add);         // Spins off and returns job

    post("/extractRdf",         ExtractTriplesController.extract);  // Spins off and returns job  - progress enabled

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
