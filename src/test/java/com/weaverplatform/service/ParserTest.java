package com.weaverplatform.service;

import com.weaverplatform.protocol.WriteOperationParser;
import com.weaverplatform.protocol.model.CreateNodeOperation;
import com.weaverplatform.service.util.SortUtil;
import com.weaverplatform.service.util.tordf.SortedWriteOperationParser;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static org.junit.Assert.assertEquals;


public class ParserTest {


  @Test
  public void test() throws IOException {
    InputStream stream = getClass().getClassLoader().getResourceAsStream("cbim.json");
    WriteOperationParser parser = new WriteOperationParser();
    assert(parser.parseNext(stream, 1).get(0) instanceof CreateNodeOperation);
    assert(parser.parseNext(stream, 0).isEmpty());
    parser.parseNext(stream, 10);
  }
  @Test
  public void testStartChars() throws IOException {
    InputStream stream = getClass().getClassLoader().getResourceAsStream("cbim.json");
    Set<Character> res = SortedWriteOperationParser.startChars(stream);
    for(Character c : res) {
      System.out.println(c);
    }
  }

  @Test
  public void testSorter() {
    assertEquals(1, SortUtil.compareReverseIndex("a", "", 0));
    assertEquals(0, SortUtil.compareReverseIndex("a", "a", 0));
    assertEquals(-1, SortUtil.compareReverseIndex("a", "aa", 0));
    assertEquals(1, SortUtil.compareReverseIndex("aa", "a", 0));
    assertEquals(-1, SortUtil.compareReverseIndex("", "a", 0));
    assertEquals(-1, SortUtil.compareReverseIndex("", "aa", 0));
  }
}
