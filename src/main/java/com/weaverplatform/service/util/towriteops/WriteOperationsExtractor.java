package com.weaverplatform.service.util.towriteops;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.weaverplatform.protocol.model.SuperOperation;
import com.weaverplatform.sdk.Weaver;
import com.weaverplatform.service.Application;
import com.weaverplatform.service.controllers.StoreController;
import com.weaverplatform.service.payloads.ExtractTriplesRequest;
import com.weaverplatform.service.payloads.JobReport;
import com.weaverplatform.service.util.Props;
import com.weaverplatform.service.util.ZipWriter;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLParser;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * @author bastbijl, Sysunite 2018
 */
public class WriteOperationsExtractor {

  static Logger logger = LoggerFactory.getLogger(Application.class);

  final static int WRITE_BATCH_SIZE = Props.getInt("WRITE_BATCH_SIZE", "service.operations.chunksize");
  final static Boolean LOG_WRITE_OPERATIONS = Props.getBoolean("LOG_WRITE_OPERATIONS", "service.operations.log");

  public static Gson gson = new GsonBuilder().setPrettyPrinting().create();




  public static WriteOperationsModel loadModel(ExtractTriplesRequest config, Weaver weaver, JobReport job) {
    InputStream containerFileStream;
    try {
      containerFileStream = weaver.downloadFile(config.getFileId());
      config.setFile(containerFileStream);
    } catch (IOException e) {
      job.setSuccess(false);
      job.setMessage("Failed downloading container file (fileId: "+config.getFileId()+") from local storage.");
      return null;
    }
    return loadModel(config, job);
  }

  public static WriteOperationsModel loadModel(ExtractTriplesRequest config, JobReport job) {
    return loadModel(config, job, true);
  }
  public static WriteOperationsModel loadModel(ExtractTriplesRequest config, JobReport job, boolean fromZip) {
    job.setScale(100);

    RDFParser parser;

    if("turtle".equals(config.getRdfFormat().toLowerCase())) {
      parser = new TurtleParser();
    } else if("rdf/xml".equals(config.getRdfFormat().toLowerCase())) {
      parser = new RDFXMLParser();
    } else {
      return null;
    }

    job.setProgress(10);

    WriteOperationsModel model = new WriteOperationsModel(StoreController.USER);
    Set<Namespace> additionalNamespaces = model.getNamespaces();
    for(String prefix: config.getPrefixMap().keySet()) {
      String fullUri = config.getPrefixMap().get(prefix);
      additionalNamespaces.add(new SimpleNamespace(prefix, fullUri));
    }

    model.setFilterRestrictions(false);
    model.setFilterPreferredLanguage(true);
    model.setGraph(config.getDefaultGraph());
    model.setGraphMap(config.getGraphMap());
    model.setDismissGraphs(config.getDismissGraphs());

    InputStream fileStream = null;
    try {
      if(fromZip) {
        fileStream = ZipWriter.readFromZip(config.getFile(), config.getPath());
      } else {
        fileStream = config.getFile();
      }
    } catch (IOException e) {
    }
    if(fileStream != null) {
      model.readStream(fileStream, "", parser);
    } else {
      String message = "Requested stream for path "+config.getPath() + " could not be found in container file.";
      logger.error(message);
      job.setSuccess(false);
      job.setMessage(message);
      return null;
    }

    job.setProgress(20);

    return model;
  }



  public static void writeOperationsToStore(WriteOperationsModel model, Weaver weaver, JobReport job) {

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

        if(LOG_WRITE_OPERATIONS) {
          try {
            File file = new File("/tmp/" + job.getJobId() + "/bunch_" + total + ".json.gz");
            file.getParentFile().mkdirs();
            Writer writer = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)), "UTF-8");
            writer.write(gson.toJson(element.getAsJsonArray()));
            writer.flush();
            writer.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

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
  public static void writeOperations(WriteOperationsModel model, OutputStream outputStream, JobReport job) {

    JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream));

    int total = 0;
    job.setScale(model.size());
    try {
      writer.beginArray();
      while (model.hasNext()) {

        List<SuperOperation> items = model.next(WRITE_BATCH_SIZE);
        if (items.isEmpty()) {
          continue;
        }
        total += items.size();
        job.setProgress(total);
        gson.toJson(items, new TypeToken<List<SuperOperation>>() {}.getType(), writer);
      }
      job.setScale(total);
      job.setSuccess(true);
      logger.info("Inserted " + total + " write operations");
      writer.endArray();
      writer.close();
    } catch (RuntimeException e) {
      job.setSuccess(false);
      job.setMessage(e.getMessage());
    } catch (IOException e) {
      job.setSuccess(false);
      job.setMessage(e.getMessage());
    }
  }

}
