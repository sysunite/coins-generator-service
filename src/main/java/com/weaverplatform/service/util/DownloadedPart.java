package com.weaverplatform.service.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Part;
import java.io.*;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

/**
 * @author bastbijl, Sysunite 2018
 */
public class DownloadedPart implements Part {

  static Logger logger = LoggerFactory.getLogger(DownloadedPart.class);

  private File temp;

  public DownloadedPart() throws IOException {
    this.temp = File.createTempFile("writeops-", ".json.gz");
    logger.info("Storing writeops-json temporarily at "+this.temp.getPath());
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new GZIPInputStream(new FileInputStream(temp));
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

  public void writeZippedStream(InputStream zippedStream) throws IOException {
    FileOutputStream outputStream = new FileOutputStream(temp);
    IOUtils.copy(zippedStream, outputStream);
    zippedStream.close();
    outputStream.close();
  }

  @Override
  public void delete() throws IOException {
    temp.delete();
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
