package com.weaverplatform.service.util;

import com.weaverplatform.protocol.WriteOperationParser;
import com.weaverplatform.protocol.model.WriteOperation;
import com.weaverplatform.service.RDFXMLBasePrettyWriter;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2018
 */
public class RdfWriter {

  private static final int CHUNK_SIZE = 5000;

  public static void write(InputStream input, OutputStream output, Map<String, String> prefixMap, String mainContext, String defaultPrefix) throws IOException {

    // Reader
    WriteOperationParser parser = new WriteOperationParser();
    CoinsMapper mapper = new CoinsMapper(mainContext, defaultPrefix, prefixMap);

    // Writer
    RDFXMLBasePrettyWriter writer = new RDFXMLBasePrettyWriter(output);
//    RDFXMLWriter writer = new RDFXMLWriter(output);
    writer.setBase(mainContext);
    writer.handleNamespace("", mainContext+"#");
    for(String prefix : prefixMap.keySet()) {
      writer.handleNamespace(prefix, prefixMap.get(prefix));
    }

    // Start looping
    List<WriteOperation> operations = parser.parseNext(input, CHUNK_SIZE);
    writer.startRDF();
    while(!operations.isEmpty()) {

      for(WriteOperation operation : operations) {
        Statement statement = mapper.write(operation);
        if(statement != null) {
          writer.handleStatement(statement);
        }
      }
      operations = parser.parseNext(input, CHUNK_SIZE);
    }
    writer.endRDF();
  }

  public static void writeTtl(InputStream input, OutputStream output, Map<String, String> prefixMap, String mainContext, String defaultPrefix) throws IOException {

    // Reader
    WriteOperationParser parser = new WriteOperationParser();
    CoinsMapper mapper = new CoinsMapper(mainContext, defaultPrefix, prefixMap);

    // Writer
    TurtleWriter writer = new TurtleWriter(output);
    writer.handleNamespace("", mainContext+"#");
    for(String prefix : prefixMap.keySet()) {
      writer.handleNamespace(prefix, prefixMap.get(prefix));
    }

    // Start looping
    List<WriteOperation> operations = parser.parseNext(input, CHUNK_SIZE);
    writer.startRDF();
    while(!operations.isEmpty()) {

      for(WriteOperation operation : operations) {
        Statement statement = mapper.write(operation);
        if(statement != null) {
          writer.handleStatement(statement);
        }
      }
      operations = parser.parseNext(input, CHUNK_SIZE);
    }
    writer.endRDF();
  }
}
