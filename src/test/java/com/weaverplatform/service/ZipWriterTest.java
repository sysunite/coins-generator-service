package com.weaverplatform.service;

import com.weaverplatform.service.util.ZipWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;

public class ZipWriterTest {

  @Test
  public void addRdf() throws IOException {
    InputStream stream = ZipWriterTest.class.getClassLoader().getResourceAsStream("big.json");
    String zipKey = "abc";
    HashMap<String, String> prefixMap = new HashMap<>();
    prefixMap.put("", "http://ins#");
    prefixMap.put("actuele-wegenlijst", "http://otl.rws.nl/actuele-wegenlijst#");
    prefixMap.put("coins-base", "http://www.coinsweb.nl/cbim-2.0.rdf#");
    prefixMap.put("cbim", "http://www.coinsweb.nl/cbim-2.0.rdf#");
    prefixMap.put("coins-referentie-rws", "http://otl.rws.nl/coins2/rws-referentiekader.rdf#");
    prefixMap.put("lcr-rf", "http://otl.rws.nl/lcr-rf#");
    prefixMap.put("mc-map-coins", "http://otl.rws.nl/mc-map-coins#");
    prefixMap.put("mc-mi-types", "http://otl.rws.nl/mc-mi-types#");
    prefixMap.put("mc-mt0006", "http://otl.rws.nl/mc-mt0006#");
    prefixMap.put("mc-rules", "http://otl.rws.nl/mc-rules#");
    prefixMap.put("mc-top", "http://otl.rws.nl/mc-top#");
    prefixMap.put("MD0295", "http://otl.rws.nl/MD0295#");
    prefixMap.put("otl-doc", "http://otl.rws.nl/otl-doc#");
    prefixMap.put("otl-doctype", "http://otl.rws.nl/otl-doctype#");
    prefixMap.put("otl-gebrek", "http://otl.rws.nl/otl-gebrek#");
    prefixMap.put("otl-m-coins-base", "http://otl.rws.nl/otl-m-coins-base#");
    prefixMap.put("otl-mat", "http://otl.rws.nl/otl-mat#");
    prefixMap.put("otl-org-nl-overheid", "http://standaarden.overheid.nl/owms/terms/Overheidsorganisatie#");
    prefixMap.put("otl-org-overige", "http://otl.rws.nl/otl-org-overige#");
    prefixMap.put("otl-orgtype", "http://otl.rws.nl/otl-orgtype#");
    prefixMap.put("otl-pc", "http://otl.rws.nl/otl-pc#");
    prefixMap.put("otl-rws-appl", "http://otl.rws.nl/otl-rws-appl#");
    prefixMap.put("otl-rws-kv", "http://otl.rws.nl/otl-rws-kv#");
    prefixMap.put("otl-rws-org", "http://otl.rws.nl/otl-rws-org#");
    prefixMap.put("otl-rws-pr", "http://otl.rws.nl/otl-rws-pr#");
    prefixMap.put("otl-svt", "http://otl.rws.nl/otl-svt#");
    prefixMap.put("otl-units-quantities", "http://otl.rws.nl/otl-units-quantities#");
    prefixMap.put("otl-vvt", "http://otl.rws.nl/otl-vvt#");
    prefixMap.put("otl-wvt", "http://otl.rws.nl/otl-wvt#");
    prefixMap.put("otl", "http://otl.rws.nl/otl#");
    prefixMap.put("owl", "http://www.w3.org/2002/07/owl#");
    prefixMap.put("primitives", "http://otl.rws.nl/primitives#");
    prefixMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    prefixMap.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    prefixMap.put("sbi2008", "http://otl.rws.nl/sbi2008#");
    prefixMap.put("xsd", "http://www.w3.org/2001/XMLSchema#");
    prefixMap.put("coins", "http://otl.rws.nl/coins#");
    prefixMap.put("rf", "http://otl.rws.nl/coins2/rws-referentiekader.rdf#");
    String mainContext = "http://dataroom";
    ZipWriter.addRdfToZip(stream, zipKey, Paths.get("otl.rdf"), prefixMap, mainContext, "");
  }

  @Test
  public void addTtl() throws IOException {
    InputStream stream = ZipWriterTest.class.getClassLoader().getResourceAsStream("big.json");
    String zipKey = "abc";
    HashMap<String, String> prefixMap = new HashMap<>();
    prefixMap.put("", "http://ins#");
    prefixMap.put("actuele-wegenlijst", "http://otl.rws.nl/actuele-wegenlijst#");
    prefixMap.put("coins-base", "http://www.coinsweb.nl/cbim-2.0.rdf#");
    prefixMap.put("cbim", "http://www.coinsweb.nl/cbim-2.0.rdf#");
    prefixMap.put("coins-referentie-rws", "http://otl.rws.nl/coins2/rws-referentiekader.rdf#");
    prefixMap.put("lcr-rf", "http://otl.rws.nl/lcr-rf#");
    prefixMap.put("mc-map-coins", "http://otl.rws.nl/mc-map-coins#");
    prefixMap.put("mc-mi-types", "http://otl.rws.nl/mc-mi-types#");
    prefixMap.put("mc-mt0006", "http://otl.rws.nl/mc-mt0006#");
    prefixMap.put("mc-rules", "http://otl.rws.nl/mc-rules#");
    prefixMap.put("mc-top", "http://otl.rws.nl/mc-top#");
    prefixMap.put("MD0295", "http://otl.rws.nl/MD0295#");
    prefixMap.put("otl-doc", "http://otl.rws.nl/otl-doc#");
    prefixMap.put("otl-doctype", "http://otl.rws.nl/otl-doctype#");
    prefixMap.put("otl-gebrek", "http://otl.rws.nl/otl-gebrek#");
    prefixMap.put("otl-m-coins-base", "http://otl.rws.nl/otl-m-coins-base#");
    prefixMap.put("otl-mat", "http://otl.rws.nl/otl-mat#");
    prefixMap.put("otl-org-nl-overheid", "http://standaarden.overheid.nl/owms/terms/Overheidsorganisatie#");
    prefixMap.put("otl-org-overige", "http://otl.rws.nl/otl-org-overige#");
    prefixMap.put("otl-orgtype", "http://otl.rws.nl/otl-orgtype#");
    prefixMap.put("otl-pc", "http://otl.rws.nl/otl-pc#");
    prefixMap.put("otl-rws-appl", "http://otl.rws.nl/otl-rws-appl#");
    prefixMap.put("otl-rws-kv", "http://otl.rws.nl/otl-rws-kv#");
    prefixMap.put("otl-rws-org", "http://otl.rws.nl/otl-rws-org#");
    prefixMap.put("otl-rws-pr", "http://otl.rws.nl/otl-rws-pr#");
    prefixMap.put("otl-svt", "http://otl.rws.nl/otl-svt#");
    prefixMap.put("otl-units-quantities", "http://otl.rws.nl/otl-units-quantities#");
    prefixMap.put("otl-vvt", "http://otl.rws.nl/otl-vvt#");
    prefixMap.put("otl-wvt", "http://otl.rws.nl/otl-wvt#");
    prefixMap.put("otl", "http://otl.rws.nl/otl#");
    prefixMap.put("owl", "http://www.w3.org/2002/07/owl#");
    prefixMap.put("primitives", "http://otl.rws.nl/primitives#");
    prefixMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    prefixMap.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    prefixMap.put("sbi2008", "http://otl.rws.nl/sbi2008#");
    prefixMap.put("xsd", "http://www.w3.org/2001/XMLSchema#");
    prefixMap.put("coins", "http://otl.rws.nl/coins#");
    prefixMap.put("rf", "http://otl.rws.nl/coins2/rws-referentiekader.rdf#");
    String mainContext = "http://dataroom";
    ZipWriter.addTtlToZip(stream, zipKey, Paths.get("bimdee/big.ttl"), prefixMap, mainContext, "");
  }

  @Test
  public void addFile() throws IOException {
    InputStream stream = ZipWriterTest.class.getClassLoader().getResourceAsStream("tree.jpg");
    String zipKey = "abc";
    ZipWriter.addToZip(stream, zipKey, Paths.get("x/tree.jpg"));
  }
}
