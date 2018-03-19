package com.weaverplatform.service.util;

import com.weaverplatform.protocol.SortedWriteOperationParser;
import com.weaverplatform.protocol.model.SuperOperation;
import com.weaverplatform.service.RDFXMLBasePrettyWriter;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * @author bastbijl, Sysunite 2018
 */
public class RdfWriter {

  static Logger logger = LoggerFactory.getLogger(RdfWriter.class);

  final static int CHUNK_SIZE = Props.getInt("CHUNK_SIZE", "rdfwriter.chunksize");

  public static void write(List<Part> parts, AbstractRDFWriter writer, Map<String, String> prefixMap, String mainContext, String defaultPrefix) {

    Set<Character> filterChars = new HashSet<>();
    for(Part part : parts) {
      try (InputStream inputStream = part.getInputStream()) {
        filterChars.addAll(SortedWriteOperationParser.startChars(inputStream));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    CoinsMapper mapper = new CoinsMapper(mainContext, defaultPrefix, prefixMap);
    writer.startRDF();

    for(Character filterChar : filterChars) {

      for(Part part : parts) {
        SortedWriteOperationParser parser = new SortedWriteOperationParser();
        try (InputStream inputStream = part.getInputStream()) {

          // Start looping
          TreeSet<SuperOperation> operations = parser.parseNextSorted(inputStream, CHUNK_SIZE, filterChar);

          while (!operations.isEmpty()) {
            logger.info("Got "+operations.size()+" when looking for filter char '"+filterChar+"'");

            if (operations.size() >= CHUNK_SIZE) {
              throw new RuntimeException("Please try to increase chunk size, not able to sort like this");
            }

            for (SuperOperation operation : operations) {
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
    }
    writer.endRDF();
  }

  public static void writeXml(List<Part> sources, OutputStream output, Map<String, String> prefixMap, String mainContext, String defaultPrefix) {

    RDFXMLBasePrettyWriter writer = new RDFXMLBasePrettyWriter(output);
    writer.setBase(mainContext);
    writer.handleNamespace("", mainContext+"#");
    for(String prefix : prefixMap.keySet()) {
      writer.handleNamespace(prefix, prefixMap.get(prefix));
    }
    write(sources, writer, prefixMap, mainContext, defaultPrefix);
    try {
      output.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void writeTtl(List<Part> sources, OutputStream output, Map<String, String> prefixMap, String mainContext, String defaultPrefix) {

    TurtleWriter writer = new TurtleWriter(output);
    writer.handleNamespace("", mainContext+"#");
    for(String prefix : prefixMap.keySet()) {
      writer.handleNamespace(prefix, prefixMap.get(prefix));
    }
    write(sources, writer, prefixMap, mainContext, defaultPrefix);
    try {
      output.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
