package com.weaverplatform.service.controllers;


import com.weaverplatform.service.util.ZipWriter;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

public class ContainerController {

  public static Route addRdf = (Request request, Response response) -> {
    String zipKey = request.params("zipKey");
    AddTriplesRequest config = AddTriplesRequest.from(request);
    ZipWriter.addRdfToZip(zipKey, config);
    return null;
  };

  public static Route addTtl = (Request request, Response response) -> {
    String zipKey = request.params("zipKey");
    AddTriplesRequest config = AddTriplesRequest.from(request);
    ZipWriter.addTtlToZip(zipKey, config);
    return null;
  };

  public static Route addFile = (Request request, Response response) -> {
    String zipKey = request.params("zipKey");
    AddFileRequest config = AddFileRequest.from(request);
    ZipWriter.addToZip(zipKey, config);
    return null;
  };

  public static Route download = (Request request, Response response) -> {

    String zipKey = request.params("zipKey");

    // Read the resulting file
    response.raw().setContentType("application/octet-stream");
    response.raw().setHeader("Content-Disposition","container; filename=container.ccr");

    HttpServletResponse raw = response.raw();
    try (OutputStream stream = raw.getOutputStream()) {
      ZipWriter.streamDownload(zipKey, stream);
    } catch(Exception e) {
      response.status(500);
      response.body(e.getMessage());
    }

    return response.raw();
  };

  public static Route wipe = (Request request, Response response) -> {

    String zipKey = request.params("zipKey");

    ZipWriter.wipe(zipKey);

    return null;
  };

}
