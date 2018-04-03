package com.weaverplatform.service.util;

import com.weaverplatform.sdk.Weaver;
import com.weaverplatform.sdk.WeaverFile;
import com.weaverplatform.service.Application;
import com.weaverplatform.service.RDFXMLBasePrettyWriter;
import com.weaverplatform.service.payloads.AddFileRequest;
import com.weaverplatform.service.payloads.AddTriplesRequest;
import com.weaverplatform.service.payloads.JobReport;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author bastbijl, Sysunite 2018
 */
public class ZipWriter {

  static Logger logger = LoggerFactory.getLogger(Application.class);

  private static final int BLOCK_RETRY_TIME_MS = 3000;
  private static final int BLOCK_RETRIES = 10;

  private static HashSet<String> blockList = new HashSet<>();

  public static void resetBlockList() {
    blockList = new HashSet<>();
  }

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

  public static void addRdfToZip(String zipKey, AddTriplesRequest config, JobReport job) {

    logger.info("Will add rdf to zip "+zipKey);
    try {
      try (FileSystem fs = prepareZip(zipKey, config.getPath())) {
        Path nf = fs.getPath(config.getPath());
        try (OutputStream output = Files.newOutputStream(nf, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {

          RDFWriter writer;
          if("turtle".equals(config.getRdfFormat().toLowerCase())) {
            writer = new TurtleWriter(output);
          } else if("rdf/xml".equals(config.getRdfFormat().toLowerCase())) {
            writer = new RDFXMLBasePrettyWriter(output);
          } else {
            return;
          }

          RdfWriter.write(config, writer);
          output.flush();
          job.setSuccess(true);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      freeZipKey(zipKey);
    }
  }


  public static InputStream readFromZip(InputStream input, String path) throws IOException {
    ZipInputStream zipStream = new ZipInputStream(input);
    while(zipStream.available() > 0) {
      ZipEntry entry = zipStream.getNextEntry();

      if (entry.getName().equals(path)) {
        logger.info("Found entry with name " + path + " with size: "+entry.getSize());
        return zipStream;
      }
    }
    return null;
  }

  private static synchronized File requestAccess(String zipKey) {
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

  public static void streamDownload(String zipKey, OutputStream stream) {
    File file = requestAccess(zipKey);
    try {
      Files.copy(file.toPath(), stream);
      stream.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    freeZipKey(zipKey);
  }

  public static void streamDownload(String zipKey, Weaver weaver, JobReport job) {
    File file = requestAccess(zipKey);
    try {
      FileInputStream input = new FileInputStream(file);
      WeaverFile weaverFile = weaver.uploadFile(input, zipKey + ".ccr");
      job.setFileId(weaverFile.getId());
      job.setSuccess(true);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
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
