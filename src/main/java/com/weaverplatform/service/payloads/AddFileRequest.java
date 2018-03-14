package com.weaverplatform.service.payloads;

import java.io.InputStream;

/**
 * @author bastbijl, Sysunite 2018
 */
public interface AddFileRequest {

  void setFile(InputStream file);

  InputStream getFile();

  void setPath(String path);

  String getPath();

}
