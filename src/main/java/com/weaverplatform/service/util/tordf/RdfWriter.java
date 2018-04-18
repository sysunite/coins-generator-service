package com.weaverplatform.service.util.tordf;

import com.weaverplatform.protocol.model.SuperOperation;
import com.weaverplatform.service.RDFXMLBasePrettyWriter;
import com.weaverplatform.service.payloads.AddTriplesRequest;
import com.weaverplatform.service.util.Props;
import com.weaverplatform.service.util.towriteops.SortedWriteOperationParser;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static com.weaverplatform.service.util.SortUtil.charComparator;

/**
 * @author bastbijl, Sysunite 2018
 */
public class RdfWriter {

  static Logger logger = LoggerFactory.getLogger(RdfWriter.class);

  final static int CHUNK_SIZE = Props.getInt("CHUNK_SIZE", "rdfwriter.chunksize");

  public static void write(AddTriplesRequest config, RDFWriter writer) {

    if(writer instanceof RDFXMLBasePrettyWriter) {
      ((RDFXMLBasePrettyWriter)writer).setBase(config.getMainContext());
    }
    writer.handleNamespace("", config.getMainContext()+"#");
    for(String prefix : config.getPrefixMap().keySet()) {
      writer.handleNamespace(prefix, config.getPrefixMap().get(prefix));
    }

    Set<Character> filterChars = new TreeSet<>(charComparator);
    for(Part part : config.getPayloads()) {
      try (InputStream inputStream = part.getInputStream()) {
        for(char ch : SortedWriteOperationParser.startChars(inputStream)) {
          if(!filterChars.contains(ch)) {
            filterChars.add(ch);
          }
        }
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


    HashSet<String> dismissKeys = config.getDismissKeys();
    if(dismissKeys == null) {
      dismissKeys = new HashSet<>();
    }

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
              if (operation.getKey() == null || dismissKeys.contains(operation.getKey())) {
                continue;
              }
              for(Statement statement : mapper.map(operation)) {
                if (statement == null) {
                  continue;
                }
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
}
