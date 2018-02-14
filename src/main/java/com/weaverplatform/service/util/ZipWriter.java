package com.weaverplatform.service.util;

import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author bastbijl, Sysunite 2018
 */
public class ZipWriter {

  public static void addToZip(InputStream stream, String zipKey, Path zipPath) throws IOException {

    try (FileSystem fs = prepareZip(zipKey, zipPath)) {
      Path nf = fs.getPath(zipPath.toString());
      try (OutputStream out = Files.newOutputStream(nf)) {
        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = stream.read(buffer)) != -1)
          out.write(buffer, 0, bytesRead);
      }
    }
  }

  public static void addRdfToZip(InputStream stream, String zipKey, Path zipPath, Map<String, String> prefixMap, String mainContext, String defaultPrefix) throws IOException {

    try (FileSystem fs = prepareZip(zipKey, zipPath)) {
      Path nf = fs.getPath(zipPath.toString());
      try (OutputStream out = Files.newOutputStream(nf)) {
        RdfWriter.write(stream, out, prefixMap, mainContext, defaultPrefix);
      }
    }
  }
  public static void addTtlToZip(InputStream stream, String zipKey, Path zipPath, Map<String, String> prefixMap, String mainContext, String defaultPrefix) throws IOException {

    try (FileSystem fs = prepareZip(zipKey, zipPath)) {
      Path nf = fs.getPath(zipPath.toString());
      try (OutputStream out = Files.newOutputStream(nf, StandardOpenOption.TRUNCATE_EXISTING)) {
        RdfWriter.writeTtl(stream, out, prefixMap, mainContext, defaultPrefix);
      }
    }
  }

  private static FileSystem prepareZip(String zipKey, Path zipPath) throws IOException {
    File file = new File("/tmp/"+zipKey+".ccr");

    if(file.exists()) {
      if(zipEntries(file).contains(zipPath)) {
//        throw new RuntimeException("ZipPath "+zipPath.toString()+" already stored in zip.");
      }
    }

    Map<String, String> env = new HashMap<>();
    env.put("create", "true");
    URI uri = URI.create("jar:" + file.toPath().toUri());
    FileSystem fs = FileSystems.newFileSystem(uri, env);
    Path nf = fs.getPath(zipPath.toString());
    Path parent = nf.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }

    return fs;
  }

  public static List<Path> zipEntries(File file) throws IOException {

    ArrayList<Path> list = new ArrayList();

//    // See if this finds parsing problems
//    new ZipFile(file);

    // Get the zip file content
    ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
    ZipEntry ze = zis.getNextEntry();

    while(ze != null) {

      // Skip directories
      if(ze.isDirectory()) {
        ze = zis.getNextEntry();
        continue;
      }

      Path zipPath = Paths.get(FilenameUtils.separatorsToUnix(ze.getName()));
      list.add(zipPath);

      ze = zis.getNextEntry();
    }

    zis.closeEntry();
    zis.close();

    return list;
  }
}
