package run.innkeeper.services;

import run.innkeeper.utilities.Logging;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AdminUIFileSystem{
  static AdminUIFileSystem adminUIFileSystem = new AdminUIFileSystem();
  Map<String, String> files = new HashMap<>();

  public void load() throws IOException {
    String adminRepo = "https://github.com/InnKeeperDevOps/admin-ui/archive/main.zip";
    files.putAll(this.downloadAndExtractZip(adminRepo));
    Logging.info("Loaded "+getFiles().size()+" Admin UI files, totaling "+getFiles().values().stream().map(file->file.length()).reduce(0, (a, b)->a+b).intValue()+" bytes");
  }

  private Map<String, String> downloadAndExtractZip(String zipUrl) throws IOException {
    Map<String, String> fileContents = new HashMap<>();

    URL url = new URL(zipUrl);
    try (InputStream in = new BufferedInputStream(url.openStream());
         ZipInputStream zipIn = new ZipInputStream(in)) {
      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
        if (!entry.isDirectory()) {
          String fileName = entry.getName();
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          byte[] buffer = new byte[1024];
          int len;
          while ((len = zipIn.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
          }
          String fileContent = baos.toString("UTF-8");
          fileContents.put(fileName, fileContent);
        }
        zipIn.closeEntry();
      }
    }

    return fileContents;
  }

  public static AdminUIFileSystem get() {
    return adminUIFileSystem;
  }

  public Map<String, String> getFiles() {
    return files;
  }
}
