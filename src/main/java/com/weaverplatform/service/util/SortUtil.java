package com.weaverplatform.service.util;

import com.weaverplatform.protocol.model.SuperOperation;

import java.util.Comparator;

/**
 * @author bastbijl, Sysunite 2018
 */
public class SortUtil {

  public static String keyFilter(String key) {
    if(key.equals("rdf:type")) {
      return "a";
    }
    return key;
  }

  public static String sortKey(SuperOperation operation) {
    if(operation.getAction().equals("create-attribute")) {
      return operation.getId() + keyFilter(operation.getKey()) + operation.getSourceId();
    }
    if(operation.getAction().equals("create-relation")) {
      return operation.getTargetId() + keyFilter(operation.getKey()) + operation.getSourceId();
    }
    return operation.getId();
  }

  public static Comparator<Character> charComparator = new Comparator<Character>() {
    @Override
    public int compare(Character o1, Character o2) {
      return compareChar(o1, o2);
    }
  };

  public static Comparator<SuperOperation> superOperationComparator = new Comparator<SuperOperation>() {
    @Override
    public int compare(SuperOperation o1, SuperOperation o2) {
      String key1 = sortKey(o1);
      String key2 = sortKey(o2);
      return compareReverseIndex(key1, key2, 0);
    }
  };

  public static int compareReverseIndex(String key1, String key2, int reverseIndex) {
    int i1 = key1.length() - reverseIndex - 1;
    int i2 = key2.length() - reverseIndex - 1;
    if(i1 > -1 && i2 > -1) {
      char c1 = key1.charAt(i1);
      char c2 = key2.charAt(i2);
      if(c1 == c2) {
        return compareReverseIndex(key1, key2, reverseIndex + 1);

      } else {
        return compareChar(c1, c2);
      }
    }
    if(i1 == -1 && i2 == -1) {
      return 0;
    }
    if(i1 == -1) {
      return -1;
    }
    return 1;
  }

  public static int compareChar(char c1, char c2) {
    if(c1 == c2) {
      return 0;

      // Special case for ':'
    } else if(c1 == ':') {
      return -1;
    } else if(c2 == ':') {
      return 1;

    } else {
      return c1 - c2;
    }
  }
}
