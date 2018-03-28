package com.weaverplatform.service.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.weaverplatform.protocol.model.SuperOperation;
import com.weaverplatform.protocol.model.WriteOperation;
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

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * @author bastbijl, Sysunite 2018
 */
public class WriteOperationsExtractor {

  static Logger logger = LoggerFactory.getLogger(Application.class);

  public static final int WRITE_BATCH_SIZE = 1000;

  public static Gson gson = new Gson();

  public static void writeOperations(ExtractTriplesRequest config, Weaver weaver, JobReport job) {

    RDFParser parser;

    if("turtle".equals(config.getRdfFormat().toLowerCase())) {
      parser = new TurtleParser();
    } else if("rdf/xml".equals(config.getRdfFormat().toLowerCase())) {
      parser = new RDFXMLParser();
    } else {
      return;
    }

    List<Part> inputs = config.getPayloads();

    WriteOperationsModel model = new WriteOperationsModel(StoreController.USER);
    Set<Namespace> additionalNamespaces = model.getNamespaces();
    for(String prefix: config.getPrefixMap().keySet()) {
      additionalNamespaces.add(new SimpleNamespace(prefix, config.getPrefixMap().get(prefix)));
    }

    model.setFilterRestrictions(false);
    model.setFilterPreferredLanguage(true);
    model.setGraph(config.getToGraph());

    for(Part input : inputs) {
      InputStream fileStream = null;
      try {
        fileStream = ZipWriter.readFromZip(input.getInputStream(), config.getPath());
      } catch (IOException e) {
      }
      if(fileStream != null) {
        model.readStream(fileStream, "", parser);
      } else {
        logger.error("Requested stream for path "+config.getPath() + " could not be found in container file.");
      }
    }
    writeTo(model, weaver);
    job.setSuccess(true);
  }


  private static void writeTo(WriteOperationsModel model, Weaver weaver) {

    int total = 0;
    while(model.hasNext()) {

      List<WriteOperation> items = model.next(WRITE_BATCH_SIZE);
      if(items.isEmpty()) {
        continue;
      }
      total += items.size();

      JsonElement element = gson.toJsonTree(items, new TypeToken<List<SuperOperation>>() {}.getType());
      weaver.reallySendCreate(element.getAsJsonArray(), false);
    }

    logger.info("Inserted " + total + " write operations");
  }
}
