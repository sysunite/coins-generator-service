package com.weaverplatform.service.util;

import com.weaverplatform.protocol.SortedWriteOperationParser;
import com.weaverplatform.protocol.model.SuperOperation;
import com.weaverplatform.service.RDFXMLBasePrettyWriter;
import com.weaverplatform.service.payloads.AddTriplesRequest;
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

  public static void write(AddTriplesRequest config, AbstractRDFWriter writer) {

    Set<Character> filterChars = new TreeSet<>();
    for(Part part : config.getPayloads()) {
      try (InputStream inputStream = part.getInputStream()) {
        filterChars.addAll(SortedWriteOperationParser.startChars(inputStream));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    CoinsMapper mapper;
    if(config.reify()) {
      mapper = new ReifiedCoinsMapper(config.getMainContext(), config.getDefaultPrefix(), config.getPrefixMap());
    } else {
      mapper = new CoinsMapper(config.getMainContext(), config.getDefaultPrefix(), config.getPrefixMap());
    }
    writer.startRDF();

    for(Character filterChar : filterChars) {

      for(Part part : config.getPayloads()) {
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
              for(Statement statement : mapper.map(operation)) {
                if (statement != null) {
                  writer.handleStatement(statement);
                }
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

  public static void writeXml(AddTriplesRequest config, OutputStream output) {

    RDFXMLBasePrettyWriter writer = new RDFXMLBasePrettyWriter(output);
    writer.setBase(config.getMainContext());
    writer.handleNamespace("", config.getMainContext()+"#");
    for(String prefix : config.getPrefixMap().keySet()) {
      writer.handleNamespace(prefix, config.getPrefixMap().get(prefix));
    }
    write(config, writer);
    try {
      output.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void writeTtl(AddTriplesRequest config, OutputStream output) {

    TurtleWriter writer = new TurtleWriter(output);
    writer.handleNamespace("", config.getMainContext()+"#");
    for(String prefix : config.getPrefixMap().keySet()) {
      writer.handleNamespace(prefix, config.getPrefixMap().get(prefix));
    }
    write(config, writer);
    try {
      output.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
