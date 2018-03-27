package com.weaverplatform.service.util;

import com.weaverplatform.protocol.model.SuperOperation;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReifiedCoinsMapper extends CoinsMapper {

  private static Logger log = LoggerFactory.getLogger(ReifiedCoinsMapper.class);

  public ReifiedCoinsMapper(String context, String defaultPrefix, Map uris) {
    super(context, defaultPrefix, uris);
  }


  public List<Statement> map(SuperOperation operation) {

    ArrayList<Statement> list = new ArrayList<>();

    if("create-node".equals(operation.getAction())) {
      return list;
    }

    if("create-attribute".equals(operation.getAction())) {
      list.add(createStatement(          getIRI(operation.getId())));
      list.add(createStatementSubject(   getIRI(operation.getId()), getIRI(operation.getSourceId())));
      list.add(createStatementPredicate( getIRI(operation.getId()), getIRI(operation.getKey())));
      list.add(createStatementObject(    getIRI(operation.getId()), createValue(operation.getValue(), operation.getDatatype())));

      if (operation.isReplacing()) {
        throw new RuntimeException("Removes or replaces not allowed");
      }
      return list;
    }

    if("create-relation".equals(operation.getAction())) {
      list.add(createStatement(          getIRI(operation.getId())));
      list.add(createStatementSubject(   getIRI(operation.getId()), getIRI(operation.getSourceId())));
      list.add(createStatementPredicate( getIRI(operation.getId()), getIRI(operation.getKey())));
      list.add(createStatementObject(    getIRI(operation.getId()), getIRI(operation.getTargetId())));

      if (operation.isReplacing()) {
        throw new RuntimeException("Removes or replaces not allowed");
      }
      return list;
    }

    throw new RuntimeException("This operation is not supported: "+operation.getAction());
  }


  private Statement createStatement(IRI id) {
    return valueFactory.createStatement(id, RDF.TYPE, RDF.STATEMENT, context);
  }

  private Statement createStatementSubject(IRI id, IRI subject) {
    return valueFactory.createStatement(id, RDF.SUBJECT, subject, context);
  }

  private Statement createStatementPredicate(IRI id, IRI predicate) {
    return valueFactory.createStatement(id, RDF.PREDICATE, predicate, context);
  }

  private Statement createStatementObject(IRI id, Value object) {
    return valueFactory.createStatement(id, RDF.OBJECT, object, context);
  }
}
