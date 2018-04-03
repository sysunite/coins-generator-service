package com.weaverplatform.service.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.weaverplatform.protocol.model.SuperOperation;
import com.weaverplatform.sdk.Weaver;
import com.weaverplatform.service.Application;
import com.weaverplatform.service.controllers.StoreController;
import com.weaverplatform.service.payloads.ExtractTriplesRequest;
import com.weaverplatform.service.payloads.JobReport;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLParser;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * @author bastbijl, Sysunite 2018
 */
public class WriteOperationsExtractor {

  static Logger logger = LoggerFactory.getLogger(Application.class);

  public static final int WRITE_BATCH_SIZE = 5000;

  public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public static void writeOperations(ExtractTriplesRequest config, Weaver weaver, JobReport job) {

    job.setScale(100);

    RDFParser parser;

    if("turtle".equals(config.getRdfFormat().toLowerCase())) {
      parser = new TurtleParser();
    } else if("rdf/xml".equals(config.getRdfFormat().toLowerCase())) {
      parser = new RDFXMLParser();
    } else {
      return;
    }

    InputStream containerFileStream;
    try {
      containerFileStream = weaver.downloadFile(config.getFileId());
    } catch (IOException e) {
      job.setSuccess(false);
      job.setMessage("Failed downloading container file (fileId: "+config.getFileId()+") from local storage.");
      return;
    }

    job.setProgress(10);

    WriteOperationsModel model = new WriteOperationsModel(StoreController.USER);
    Set<Namespace> additionalNamespaces = model.getNamespaces();
    for(String prefix: config.getPrefixMap().keySet()) {
      additionalNamespaces.add(new SimpleNamespace(prefix, config.getPrefixMap().get(prefix)));
    }

    model.setFilterRestrictions(false);
    model.setFilterPreferredLanguage(true);
    model.setGraph(config.getToGraph());

    InputStream fileStream = null;
    try {
      fileStream = ZipWriter.readFromZip(containerFileStream, config.getPath());
    } catch (IOException e) {
    }
    if(fileStream != null) {
      model.readStream(fileStream, "", parser);
    } else {
      String message = "Requested stream for path "+config.getPath() + " could not be found in container file.";
      logger.error(message);
      job.setSuccess(false);
      job.setMessage(message);
      return;
    }

    job.setProgress(20);

    writeTo(model, weaver, job);
  }


  private static void writeTo(WriteOperationsModel model, Weaver weaver, JobReport job) {

    int total = 0;
    job.setScale(model.size());
    try {
      while (model.hasNext()) {

        List<SuperOperation> items = model.next(WRITE_BATCH_SIZE);
        if (items.isEmpty()) {
          continue;
        }
        total += items.size();
        job.setProgress(total);

        JsonElement element = gson.toJsonTree(items, new TypeToken<List<SuperOperation>>() {}.getType());

//        try {
//          File file = new File("/tmp/" + job.getJobId() + "/bunch_" + total + ".json");
//          file.getParentFile().mkdirs();
//          FileWriter fileWriter = new FileWriter(file);
//          fileWriter.write(gson.toJson(element.getAsJsonArray()));
//          fileWriter.flush();
//          fileWriter.close();
//        } catch (IOException e) {
//          e.printStackTrace();
//        }

        weaver.reallySendCreate(element.getAsJsonArray(), false);
      }
      job.setScale(total);
      job.setSuccess(true);
      logger.info("Inserted " + total + " write operations");
    } catch (RuntimeException e) {
      job.setSuccess(false);
      job.setMessage(e.getMessage());
    }
  }
}
