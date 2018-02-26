package com.weaverplatform.service;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * @author bastbijl, Sysunite 2018
 */
public class PartMock implements Part {

  private String fileName;
  public PartMock(String fileName) {
    this.fileName = fileName;
  }
  @Override
  public InputStream getInputStream() throws IOException {
    return getClass().getClassLoader().getResourceAsStream(this.fileName);
  }

  @Override
  public String getContentType() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getSubmittedFileName() {
    return null;
  }

  @Override
  public long getSize() {
    return 0;
  }

  @Override
  public void write(String s) throws IOException {

  }

  @Override
  public void delete() throws IOException {

  }

  @Override
  public String getHeader(String s) {
    return null;
  }

  @Override
  public Collection<String> getHeaders(String s) {
    return null;
  }

  @Override
  public Collection<String> getHeaderNames() {
    return null;
  }
}
