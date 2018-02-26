package com.weaverplatform.service.util;

import com.weaverplatform.protocol.SortedWriteOperationParser;
import com.weaverplatform.protocol.model.SuperOperation;
import com.weaverplatform.service.RDFXMLBasePrettyWriter;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author bastbijl, Sysunite 2018
 */
public class RdfWriter {

  private static final int CHUNK_SIZE = 1000000;

  public static void write(Part input, AbstractRDFWriter writer, Map<String, String> prefixMap, String mainContext, String defaultPrefix) {

    Set<Character> filterChars = new HashSet<>();
    try(InputStream inputStream = input.getInputStream()) {
      filterChars = SortedWriteOperationParser.startChars(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }

    CoinsMapper mapper = new CoinsMapper(mainContext, defaultPrefix, prefixMap);
    writer.startRDF();

    for(Character filterChar : filterChars) {

      System.out.println(filterChar);
      SortedWriteOperationParser parser = new SortedWriteOperationParser();

      try(InputStream inputStream = input.getInputStream()) {

        // Start looping
        TreeSet<SuperOperation> operations = parser.parseNextSorted(inputStream, CHUNK_SIZE, filterChar);


        while (!operations.isEmpty()) {

          if(operations.size() >= CHUNK_SIZE) {
            System.out.println("Please try to increase chunk size, not able to sort like this");
          }

          for(SuperOperation operation : operations) {
            Statement statement = mapper.map(operation);
            if (statement != null) {
              writer.handleStatement(statement);
            }
          }

          operations = parser.parseNextSorted(inputStream, CHUNK_SIZE, filterChar);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    writer.endRDF();
  }

  public static void writeXml(Part input, OutputStream output, Map<String, String> prefixMap, String mainContext, String defaultPrefix) {

    RDFXMLBasePrettyWriter writer = new RDFXMLBasePrettyWriter(output);
    writer.setBase(mainContext);
    writer.handleNamespace("", mainContext+"#");
    for(String prefix : prefixMap.keySet()) {
      writer.handleNamespace(prefix, prefixMap.get(prefix));
    }
    write(input, writer, prefixMap, mainContext, defaultPrefix);
    try {
      output.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void writeTtl(Part input, OutputStream output, Map<String, String> prefixMap, String mainContext, String defaultPrefix) {

    TurtleWriter writer = new TurtleWriter(output);
    writer.handleNamespace("", mainContext+"#");
    for(String prefix : prefixMap.keySet()) {
      writer.handleNamespace(prefix, prefixMap.get(prefix));
    }
    write(input, writer, prefixMap, mainContext, defaultPrefix);
    try {
      output.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
