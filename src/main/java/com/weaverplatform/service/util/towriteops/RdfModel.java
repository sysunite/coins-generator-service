package com.weaverplatform.service.util.towriteops;

import com.weaverplatform.protocol.model.AttributeDataType;
import com.weaverplatform.protocol.model.PrimitiveDataType;
import com.weaverplatform.protocol.weavermodel.ModelDefinition;
import com.weaverplatform.service.util.Cuid;
import com.weaverplatform.util.DataTypeUtil;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author bastbijl, Sysunite 2017
 */
public abstract class RdfModel implements Model {

  private static final Logger log = LoggerFactory.getLogger(RdfModel.class);


  private HashMap<String, String> mapPrefix = new HashMap<>();


  final static Set<IRI> CLASS_IRIS = new HashSet<>();
  static {
    CLASS_IRIS.add(RDFS.CLASS);
    CLASS_IRIS.add(OWL.CLASS);
  }

  final static Set<IRI> RESTRICTION_IRIS = new HashSet<>();
  static {
    RESTRICTION_IRIS.add(OWL.RESTRICTION);
  }

  final static Set<IRI> SUBCLASS_IRIS = new HashSet<>();
  static {
    SUBCLASS_IRIS.add(RDFS.SUBCLASSOF);
  }

  final static Set<IRI> TYPE_IRIS = new HashSet<>();
  static {
    TYPE_IRIS.add(RDF.TYPE);
  }

  final static Set<IRI> LABEL_IRIS = new HashSet<>();
  static {
    LABEL_IRIS.add(RDFS.LABEL);
  }

  /**
   * The basic RdfModel keeps lists during the process of parsing. These are wiped
   * after the parsing is done.
   * - bnIds
   */

  protected boolean filterOutRestrictions = true;
  protected boolean onlyPreferredLanguage = false;
  protected List<Filter> filters = new ArrayList<>();

  protected String currentPrefix = null;
  protected String defaultGraph = null;
  protected HashMap<String, String>  graphMap = null;
  protected HashSet<String>  dismissGraphs = null;

  protected final HashMap<String, Item> items = new HashMap<>();

  protected HashMap<BNode, String> bnIds = null;

  protected final Set<Namespace> namespaces = new TreeSet<>();
  protected final List<Set<Namespace>> pendingNamespaceSets = new ArrayList<>();
  private HashMap<String, String> prefixMap;
  private HashSet<String> skippedPrefixes = new HashSet<>();

  final HashSet<Item> restrictionIds = new HashSet<>();

  public void setFilterRestrictions(boolean filter) {
    this.filterOutRestrictions = filter;
  }
  public void setFilterPreferredLanguage(boolean filter) {
    this.onlyPreferredLanguage = filter;
  }

  protected boolean filterRestriction(Item item) {
    if(filterOutRestrictions && restrictionIds.contains(item)) {
      return false;
    }
    return true;
  }

  public void addFilter(Filter filter) {
    filters.add(filter);
  }


  public void setMapPrefix(HashMap<String, String> mapPrefix) {
    this.mapPrefix = mapPrefix;
  }

  private String mapPrefix(String prefix) {
    if(mapPrefix.containsKey(prefix)) {
      return mapPrefix.get(prefix);
    }
    return prefix;
  }


  public String abbreviate(IRI resource) {

    String namespace = resource.getNamespace();
    String localName = resource.getLocalName();

    // Init
    if(prefixMap == null) {
      prefixMap = new HashMap();
      for(Namespace ns : namespaces) {
        if(skippedPrefixes.contains(ns.getPrefix())) {
          continue;
        }
        if(!prefixMap.containsKey(ns.getName())) {
          prefixMap.put(ns.getName(), ns.getPrefix());
        } else {
          if(!ns.getPrefix().equals(prefixMap.get(ns.getName()))) {
            skippedPrefixes.add(ns.getPrefix());
            log.warn("Prefix collision for fragment "+ns.getName()+", keeping old: "+prefixMap.get(ns.getName())+" (new: "+ns.getPrefix()+")");
          }
        }
      }
    }

    // Namespace found
    if(prefixMap.containsKey(namespace)) {
      String prefix = prefixMap.get(namespace);
      return mapPrefix(prefix) + ":" + localName;
    }

    // Full uri found
    if(prefixMap.containsKey(resource.stringValue())) {
      String prefix = prefixMap.get(resource.stringValue());
      return mapPrefix(prefix) + ":";
    }
    if(prefixMap.containsKey(resource.stringValue()+"#")) {
      String prefix = prefixMap.get(resource.stringValue()+"#");
      return mapPrefix(prefix) + ":";
    }

    // Nothing found
    return resource.stringValue();
  }

  @Override
  public Optional<Namespace> getNamespace(String prefix) {
    for (Namespace nextNamespace : namespaces) {
      if (prefix.equals(nextNamespace.getPrefix())) {
        return Optional.of(nextNamespace);
      }
    }
    return Optional.empty();
  }

  @Override
  public Set<Namespace> getNamespaces() {
    Set<Namespace> sampleRound = new TreeSet<>();
    pendingNamespaceSets.add(sampleRound);
    return sampleRound;
  }

  @Override
  public Namespace setNamespace(String prefix, String name) {
    removeNamespace(prefix);
    Namespace result = new SimpleNamespace(prefix, name);
    namespaces.add(result);
    return result;
  }

  @Override
  public void setNamespace(Namespace namespace) {
    removeNamespace(namespace.getPrefix());
    namespaces.add(namespace);
  }

  @Override
  public Optional<Namespace> removeNamespace(String prefix) {
    Optional<Namespace> result = getNamespace(prefix);
    if (result.isPresent()) {
      namespaces.remove(result.get());
    }
    return result;
  }

  private void updateNamespaces() {
    while(!pendingNamespaceSets.isEmpty()) {
      Set<Namespace> next = pendingNamespaceSets.remove(0);
      for(Namespace ns : next) {
        if(ns.getPrefix().isEmpty()) {
          namespaces.add(new SimpleNamespace(currentPrefix, ns.getName()));
        } else {
          namespaces.add(ns);
        }
      }
    }
  }

  public String getGraphForId(String abbreviated) {
    if(abbreviated.indexOf(":") > -1) {
      String prefix = abbreviated.substring(0, abbreviated.indexOf(":"));
      if(graphMap != null && graphMap.containsKey(prefix)) {
        return graphMap.get(prefix);
      }
    }
    return defaultGraph;
  }

  public Item getItem(String finalId) {
    if(items.containsKey(finalId)) {
      return items.get(finalId);
    }
    Item item = new Item(finalId, getGraphForId(finalId));
    items.put(finalId, item);
    return item;
  }

  public String getId(Resource resource) {
    if(resource instanceof BNode) {
      return getIdForBn((BNode)resource);
    }

    if(resource instanceof IRI) {
      return abbreviate((IRI)resource);
    }

    throw new RuntimeException("This type of resource was not envisioned : "+resource.getClass());
  }



  public String getName(Item item) {
    if(item.label != null) {
      return camelCase(item.label.stringValue());
    }
    if(ModelDefinition.illegalName(item.finalId)) {
      if(item.backupId == null) {
        item.backupId = "n" + Math.abs(item.hashCode());
      }
      return item.backupId;
    }
    return item.finalId;
  }

  @Override
  public final ValueFactory getValueFactory() {
    return SimpleValueFactory.getInstance();
  }

  @Override
  public final boolean add(Resource subj, IRI pred, Value obj, Resource... contexts) {
    if(contexts.length == 0) {
      return add(getValueFactory().createStatement(subj, pred, obj));
    }
    boolean result = true;
    for(Resource context : contexts) {
      result &= add(getValueFactory().createStatement(subj, pred, obj, context));
    }
    return result;
  }

  @Override
  public final boolean addAll(Collection<? extends Statement> c) {
    boolean result = true;
    for(Statement statement : c) {
      result &= add(statement);
    }
    return result;
  }



  @Override
  public final boolean add(Statement statement) {

    updateNamespaces();

    for(Filter filter : filters) {
      if(!filter.filter(statement)) {
        return false;
      }
    }

    Value object = statement.getObject();
    if(object instanceof Resource) {
      if(RDF.TYPE.equals(statement.getPredicate()) && RESTRICTION_IRIS.contains(object)) {
        restrictionIds.add(getItem(getId(statement.getSubject())));
      }
      return addResourceStatement(statement.getSubject(), statement.getPredicate(), (Resource) statement.getObject(), statement.getContext());
    }
    if(object instanceof Literal) {
      return addLiteralStatement(statement.getSubject(), statement.getPredicate(), (Literal) statement.getObject(), statement.getContext());
    }

    throw new RuntimeException("This type of node was not envisioned: "+object.getClass().getName());
  }

  public abstract boolean addResourceStatement(Resource subject, Resource predicate, Resource object, Resource context);
  public abstract boolean addLiteralStatement(Resource subject, Resource predicate, Literal object, Resource context);

  public String getIdForBn(BNode node) {

    if(currentPrefix == null) {
      throw new RuntimeException("There should be a currentPrefix");
    }

    if(bnIds.containsKey(node)) {
      return bnIds.get(node);
    } else {
      String generated = currentPrefix + ":" + Cuid.createCuid();
      bnIds.put(node, generated);
      return generated;
    }
  }

  public Object getValue(Literal literal) {
    PrimitiveDataType primitiveDataType = DataTypeUtil.primitiveDataType(getDataType(literal));
    switch (primitiveDataType) {

      case BOOLEAN:
        return literal.booleanValue();

      case DATE:
        return literal.stringValue();

      case DOUBLE:
        return literal.doubleValue();

      case STRING:
        return literal.stringValue();

      default:
        throw new RuntimeException("This primitiveDataType is not supported: "+primitiveDataType);
    }
  }

  public AttributeDataType getDataType(Literal literal) {
    return DataTypeUtil.parse(literal.getDatatype().stringValue());
  }










  public void readStream(InputStream stream, String emptyPrefix, RDFParser rdfParser) {

    String backupNameSpace = "http://example.com/backup";
    currentPrefix = emptyPrefix;
    bnIds = new HashMap<>();

    try {

      rdfParser.setRDFHandler(new StatementCollector(this));
      rdfParser.parse(new BufferedInputStream(stream), backupNameSpace);

      // Reset
      bnIds = null;
      currentPrefix = null;
      return;

    } catch (FileNotFoundException e) {
      log.error(e.getMessage(), e);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    throw new RuntimeException("Something went wrong reading a stream");
  }

  public static boolean preferOver(Literal newValue, Literal oldValue) {
    if(oldValue == null) {
      return true;
    }
    if(newValue.getLanguage().isPresent() && newValue.getLanguage().get().startsWith("nl")) {
      return true;
    }
    return false;
  }





  @Override
  public Model unmodifiable() {
    throw new RuntimeException("not implemented");
  }

  @Override
  public boolean contains(Resource subj, IRI pred, Value obj, Resource... contexts) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public Iterator<Statement> match(Resource subj, IRI pred, Value obj, Resource... contexts) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public boolean clear(Resource... context) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public boolean remove(Resource subj, IRI pred, Value obj, Resource... contexts) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public Model filter(Resource subj, IRI pred, Value obj, Resource... contexts) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public Set<Resource> subjects() {
    throw new RuntimeException("not implemented");
  }

  @Override
  public Set<IRI> predicates() {
    throw new RuntimeException("not implemented");
  }

  @Override
  public Set<Value> objects() {
    throw new RuntimeException("not implemented");
  }

  @Override
  public boolean contains(Object o) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public Iterator<Statement> iterator() {
    throw new RuntimeException("not implemented");
  }

  @Override
  public Object[] toArray() {
    throw new RuntimeException("not implemented");
  }

  @Override
  public <T> T[] toArray(T[] a) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public boolean remove(Object o) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public void clear() {
    throw new RuntimeException("not implemented");
  }

  @Override
  public boolean isEmpty() {
    throw new RuntimeException("not implemented");
  }

  @Override
  public int size() {
    throw new RuntimeException("not implemented");
  }

  public static String camelCase(String input) {
    Pattern p = Pattern.compile("[a-zA-Z]+");
    Matcher m = p.matcher(input);
    StringBuffer result = new StringBuffer();
    String word;
    while (m.find()) {
      word = m.group();
      result.append(word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase());
    }
    return result.toString();
  }


  abstract static class Filter {
    boolean filter(Statement statement) {
      return true;
    }
  }

  class Item {
    public String finalId;
    public Literal label;
    public String backupId;
    public String graphId;

    private HashMap<Item, AttributeItem> attributes = new HashMap<>();
    public AttributeItem getAttribute(Item key, List<AttributeItem> list) {
      if(attributes.containsKey(key)) {
        return attributes.get(key);
      }
      AttributeItem item = new AttributeItem();
      attributes.put(key, item);
      list.add(item);
      return item;
    }

    public Item(String finalId, String graphId) {
      this.finalId = finalId;
      this.graphId = graphId;
    }
    @Override
    public int hashCode() {
      return finalId.hashCode();
    }
  }
  class AttributeItem {
    public Item sourceId;
    public Item key;
    public Literal literal;
  }
  class RelationItem {
    public Item sourceId;
    public Item key;
    public Item toId;
  }
}
