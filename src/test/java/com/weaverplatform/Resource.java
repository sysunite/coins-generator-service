package com.weaverplatform;

import java.io.*;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Resource {
  public static InputStream getAsStream(String fileName) {
    try {
      return new BufferedInputStream(new FileInputStream(new File(Resource.class.getClassLoader().getResource(fileName).getFile())));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }
}
