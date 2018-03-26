package com.weaverplatform.service.util;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.weaverplatform.protocol.model.WriteOperation;
import com.weaverplatform.service.Application;
import com.weaverplatform.service.controllers.StoreController;
import com.weaverplatform.service.payloads.ExtractTriplesRequest;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLParser;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Part;
import java.io.*;
import java.util.List;
import java.util.Set;

/**
 * @author bastbijl, Sysunite 2018
 */
public class WriteOperationsExtractor {

  static Logger logger = LoggerFactory.getLogger(Application.class);

  public static void writeOperationsXml(OutputStream stream, ExtractTriplesRequest config) {
    execute(config, stream, new RDFXMLParser());
  }

  public static void writeOperationsTtl(OutputStream stream, ExtractTriplesRequest config) {
    execute(config, stream, new TurtleParser());
  }


  public static void execute(ExtractTriplesRequest config, OutputStream stream, RDFParser parser) {

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
    writeTo(model, stream);
  }


  private static void writeTo(WriteOperationsModel model, OutputStream output) {

    try {
      BufferedOutputStream osb = new BufferedOutputStream(output, 8 * 1024);
      JsonWriter writer = new JsonWriter(new OutputStreamWriter(osb));
      writer.setIndent("  ");
      writer.beginArray();

      int total = 0;
      while(model.hasNext()) {

        List<WriteOperation> items = model.next(1);
        if(items.isEmpty()) {
          continue;
        }
        WriteOperation item = items.get(0);
        total++;
        Gson gson = new Gson();
        gson.toJson(item, item.getClass(), writer);

      }

      writer.endArray();
      writer.close();

      logger.info("Inserted " + total + " write operations");

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
