package run.innkeeper.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import run.innkeeper.buses.GitBus;
import run.innkeeper.jobs.LatestCommitCheck;
import run.innkeeper.utilities.Logging;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class AdminUIFileSystem{
  Map<String, String> files = new HashMap<>();
  ObjectMapper objectMapper = new ObjectMapper();
  String commit = "";

  @Scheduled(fixedRate = 4000)
  public void grabNewestUI() throws IOException, InterruptedException {
    LatestCommitCheck latestCommitCheck = LatestCommitCheck.newInstance("admin-ui");
    if (latestCommitCheck.get() == null) {
      latestCommitCheck.create();
    }
    LogWatch logWatch = null;
    while (logWatch == null) {
      try {
        logWatch = latestCommitCheck.logWatch();
      } catch (Exception e) {
        Thread.sleep(10L);
      }
    }

    String commit = getCommit(logWatch);
    if (commit != null && !commit.equals(this.commit)) {
      Logging.debug("new commit: " + commit);
      this.commit = commit;
      this.load();
    }
    latestCommitCheck.delete();
  }

  public String getCommit(LogWatch logWatch) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(logWatch.getOutput()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        Pattern pattern = Pattern.compile("[a-f0-9]{40}");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
          String commit = matcher.group();
          Logging.debug("detected commit: " + commit);
          return commit;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void load() throws IOException {
    String adminRepo = "https://github.com/InnKeeperDevOps/admin-ui/archive/" + this.commit + ".zip";
    Map<String, String> newFiles = this.downloadAndExtractZip(adminRepo);
    files.clear();
    files.putAll(newFiles);
    Logging.info("Loaded " + getFiles().size() + " Admin UI files, totaling " + getFiles().values().stream().map(file -> file.length()).reduce(0, (a, b) -> a + b).intValue() + " bytes");
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

  public String getFile(String path) {
    return files.get("admin-ui-" + this.commit + "/" + path);
  }

  public Map<String, String> getFiles() {
    return files;
  }
}
