package com.weaverplatform.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.weaverplatform.Resource;
import com.weaverplatform.protocol.model.WriteOperation;
import com.weaverplatform.service.util.WriteOperationsModel;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Set;

public class WriteOperationsTest  {

  static Logger logger = LoggerFactory.getLogger(WriteOperationsTest.class);


  @Test
  public void test() {
    WriteOperationsModel model = new WriteOperationsModel("otl-importer");
    Set<Namespace> additionalNamespaces = model.getNamespaces();
    additionalNamespaces.add(new SimpleNamespace("lcr-rf", "http://otl.rws.nl/lcr-rf#"));
    additionalNamespaces.add(new SimpleNamespace("otl-pc", "http://otl.rws.nl/otl-pc#"));



    model.setFilterRestrictions(false);
    model.setFilterPreferredLanguage(true);

//    model.readStream(Resource.getAsStream("zo-coins2.0.rdf"), "coins-base", new RDFXMLParser());

    model.readStream(Resource.getAsStream("otl-2.2-ref.ttl"), "otl", new TurtleParser());
//    writeTo(model, "/tmp/otl-2.2.json");
    writeTo(model, "/tmp/otl-2.2-ref.json");

  }


  private static void writeTo(WriteOperationsModel model, String toFile) {

    logger.info("Will insert around " + model.size() + " write operations to " + toFile);

    try {
      OutputStream os = new FileOutputStream(toFile, false);
      BufferedOutputStream osb = new BufferedOutputStream(os, 8 * 1024);
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
