package com.weaverplatform.service.util;

import com.weaverplatform.service.Application;
import com.weaverplatform.service.payloads.ExtractTriplesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

/**
 * @author bastbijl, Sysunite 2018
 */
public class ZipExtracter {

  static Logger logger = LoggerFactory.getLogger(Application.class);

  public static void writeOperationsXml(OutputStream stream, ExtractTriplesRequest config) {

  }

  public static void writeOperationsTtl(OutputStream stream, ExtractTriplesRequest config) {

  }
}
