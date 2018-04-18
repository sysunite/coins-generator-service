package com.weaverplatform.service.util.tordf;

import com.weaverplatform.protocol.model.AttributeDataType;
import com.weaverplatform.protocol.model.SuperOperation;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.weaverplatform.protocol.model.AttributeDataType.RDF_LANGSTRING;

public class CoinsMapper {

  private static Logger log = LoggerFactory.getLogger(CoinsMapper.class);

  protected static ValueFactory valueFactory = SimpleValueFactory.getInstance();

  protected IRI context;
  protected String defaultPrefix;
  protected Map<String, String> uris;

  public CoinsMapper(String context, String defaultPrefix, Map uris) {
    this.context = getIRI(context);
    this.defaultPrefix = defaultPrefix;
    this.uris = uris;
  }

  public List<Statement> map(SuperOperation operation) {

    ArrayList<Statement> list = new ArrayList<>();

    if("create-node".equals(operation.getAction())) {
      return list;
    }

    if("create-attribute".equals(operation.getAction())) {
      if (operation.isReplacing()) {
        throw new RuntimeException("Removes or replaces not allowed");
      }
      list.add(createAttribute(operation));
      return list;
    }

    if("create-relation".equals(operation.getAction())) {
      if (operation.isReplacing()) {
        throw new RuntimeException("Removes or replaces not allowed");
      }
      list.add(createRelation(operation));
      return list;
    }

    throw new RuntimeException("This operation is not supported: "+operation.getAction());
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



  protected IRI getIRI(String string) {
    if(string.indexOf(":") == -1) {
      string = defaultPrefix + ":" + string;
    }
    return valueFactory.createIRI(expand(string));
  }

  private String expand(String abbreviated) {
    if(abbreviated.startsWith("http://") ||
       abbreviated.startsWith("https://") ||
       abbreviated.startsWith("file://")) {
      return abbreviated;
    }
    for(String prefix : uris.keySet()) {
      if(abbreviated.startsWith(prefix + ":")) {
        if(abbreviated.split(":").length < 2) {
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
