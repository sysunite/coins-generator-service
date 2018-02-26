package com.weaverplatform.service.util;

import com.weaverplatform.service.controllers.AddFileRequest;
import com.weaverplatform.service.controllers.AddTriplesRequest;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2018
 */
public class ZipWriter {

  private static final int BLOCK_RETRY_TIME_MS = 3000;
  private static final int BLOCK_RETRIES = 10;

  private static HashSet<String> blockList = new HashSet<>();

  public static void addToZip(String zipKey, AddFileRequest config) throws IOException {

    try (FileSystem fs = prepareZip(zipKey, config.getPath())) {
      Path nf = fs.getPath(config.getPath());
      try (OutputStream out = Files.newOutputStream(nf, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = config.getFile().read(buffer)) != -1)
          out.write(buffer, 0, bytesRead);
      }
    }
    freeZipKey(zipKey);
  }

  public static void addXmlToZip(String zipKey, AddTriplesRequest config) throws IOException {

    try (FileSystem fs = prepareZip(zipKey, config.getPath())) {
      Path nf = fs.getPath(config.getPath());
      try (OutputStream out = Files.newOutputStream(nf, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
        RdfWriter.writeXml(config.getPayload(), out, config.getPrefixMap(), config.getMainContext(), config.getDefaultPrefix());
      }
    }
    freeZipKey(zipKey);
  }

  public static void addTtlToZip(String zipKey, AddTriplesRequest config) throws IOException {

    try (FileSystem fs = prepareZip(zipKey, config.getPath())) {
      Path nf = fs.getPath(config.getPath());
      try (OutputStream out = Files.newOutputStream(nf, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
        RdfWriter.writeTtl(config.getPayload(), out, config.getPrefixMap(), config.getMainContext(), config.getDefaultPrefix());
      }
    }
    freeZipKey(zipKey);
  }

  private static File requestAccess(String zipKey) {
    int retries = 0;
    while(blockList.contains(zipKey) && retries++ < BLOCK_RETRIES) {
      try {
        Thread.sleep(BLOCK_RETRY_TIME_MS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    if(blockList.contains(zipKey)) {
      throw new RuntimeException("Zip file with zipKey "+zipKey+" is blocked forever.");
    }

    blockList.add(zipKey);
    return new File("/tmp/"+zipKey+".ccr");
  }

  private static FileSystem prepareZip(String zipKey, String path) throws IOException {

    File file = requestAccess(zipKey);

    Map<String, String> env = new HashMap<>();
    env.put("create", "true");
    URI uri = URI.create("jar:" + file.toPath().toUri());
    FileSystem fs = FileSystems.newFileSystem(uri, env);
    Path nf = fs.getPath(path);
    Path parent = nf.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }

    return fs;
  }

  public static void streamDownload(String zipKey, OutputStream stream) throws IOException {
    File file = requestAccess(zipKey);
    Files.copy(file.toPath(), stream);
    stream.flush();
    freeZipKey(zipKey);
  }

  public static void wipe(String zipKey) {
    File file = requestAccess(zipKey);
    file.delete();
    freeZipKey(zipKey);
  }

  private static void freeZipKey(String zipKey) {
    blockList.remove(zipKey);
  }

}
