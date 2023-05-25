package run.innkeeper.api;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import run.innkeeper.services.AdminUIFileSystem;
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

@SpringBootApplication
@EnableScheduling
@EnableWebSocket
public class ApiServer {
  AdminUIFileSystem adminUIFileSystem = AdminUIFileSystem.get();
  public ApiServer() throws IOException {
    adminUIFileSystem.load();
  }


}
