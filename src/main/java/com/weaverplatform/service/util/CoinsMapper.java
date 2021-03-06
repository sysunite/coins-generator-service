package com.weaverplatform.service.util;

import com.weaverplatform.protocol.model.*;
import com.weaverplatform.util.DataTypeUtil;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.weaverplatform.protocol.model.AttributeDataType.RDF_LANGSTRING;

public class CoinsMapper {

  private static Logger log = LoggerFactory.getLogger(CoinsMapper.class);

  private static ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private IRI context;
  private String defaultPrefix;
  private Map<String, String> uris;

  public CoinsMapper(String context, String defaultPrefix, Map uris) {
    this.context = getIRI(context);
    this.defaultPrefix = defaultPrefix;
    this.uris = uris;
  }







  public  Statement map(SuperOperation operation) {

    if("create-node".equals(operation.getAction())) {
      return createIndividual(operation);
    }

    if("create-attribute".equals(operation.getAction())) {
      if (operation.isReplacing()) {
        throw new RuntimeException("Removes or replaces not allowed");
      }
      return createAttribute(operation);
    }

    if("create-relation".equals(operation.getAction())) {
      if (operation.isReplacing()) {
        throw new RuntimeException("Removes or replaces not allowed");
      }
      return createRelation(operation);
    }

    if("create-relation".equals(operation.getAction()) ||
      "create-relation".equals(operation.getAction()) ||
      "create-relation".equals(operation.getAction()) ||
      "create-relation".equals(operation.getAction())) {
        throw new RuntimeException("Removes or replaces not allowed for Coins2-1 profile.");
    }

    throw new RuntimeException("This operation is not supported: "+operation.getAction());
  }

  private Statement createIndividual(SuperOperation payload) {
    return null;
  }

  private Statement createAttribute(SuperOperation payload) {
    IRI subject = getIRI(payload.getSourceId());
    IRI predicate = getIRI(payload.getKey());
    Value object = createValue(payload.getValue(), payload.getDatatype());
    return valueFactory.createStatement(subject, predicate, object, context);
  }

  private Statement createRelation(SuperOperation payload) {
    IRI subject = getIRI(payload.getSourceId());
    IRI predicate = getIRI(payload.getKey());
    IRI object = getIRI(payload.getTargetId());
    return valueFactory.createStatement(subject, predicate, object, context);
  }



  private IRI getIRI(String string) {
    if(string.indexOf(":") == -1) {
      string = defaultPrefix + ":" + string;
    }
    return valueFactory.createIRI(expand(string));
  }

  private String expand(String abbreviated) {
    if(abbreviated.startsWith("http")) {
      return abbreviated;
    }
    for(String prefix : uris.keySet()) {
      if(abbreviated.startsWith(prefix + ":")) {
        if(abbreviated.split(":").length == 1) {
          return uris.get(prefix);
        } else {
          return uris.get(prefix) + abbreviated.split(":")[1];
        }
      }
    }
    throw new RuntimeException("No prefix registered for this id: " + abbreviated);
  }

  public static Value createValue(Object value, AttributeDataType dataType) {

    if(dataType != null) {
      if(dataType == RDF_LANGSTRING) {
        return valueFactory.createLiteral((String)value);
      }

      IRI datatypeIri = valueFactory.createIRI(DataTypeUtil.getUri(dataType));
      if (value instanceof String) {
        return valueFactory.createLiteral((String) value, datatypeIri);
      }
      if (value instanceof Boolean) {
        return valueFactory.createLiteral((Boolean) value);
      }
      if (value instanceof Double) {
        return valueFactory.createLiteral((Double) value);
      }

      throw new RuntimeException("This class type for values is not supported: "+value.getClass().getSimpleName());
    } else {
      if(!(value instanceof String)) {
        throw new RuntimeException("If the dataType is omitted the value should be a String instance");
      }

      String stringValue = (String) value;

      if(stringValue.indexOf("^") != -1) {
        String valuePart = stringValue.split("\\^")[0].replace("\"", "");
        IRI dataTypeIri = valueFactory.createIRI(extendDataTypePrefix(stringValue.split("\\^")[1]));
        return valueFactory.createLiteral(valuePart, dataTypeIri);
      } else {
        return valueFactory.createLiteral(stringValue);
      }
    }
  }

  public static String extendDataTypePrefix(String dataTypeIRI) {
    dataTypeIRI = dataTypeIRI.replace("rdf:", RDF.NAMESPACE);
    dataTypeIRI = dataTypeIRI.replace("xsd:", XMLSchema.NAMESPACE);
    return dataTypeIRI;
  }
}
