package run.innkeeper.api.endpoints;

import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.annotations.UserAuthorized;
import run.innkeeper.api.annotations.WebSocketUserAuthorized;
import run.innkeeper.services.K8sService;
import run.innkeeper.utilities.Logging;

import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@Controller
@RequestMapping("/pod")
public class PodCommand{
  public static final char CTRL_C = '\u0003';

  private SimpUserRegistry simpUserRegistry;
  private SimpMessagingTemplate simpMessagingTemplate;
  private K8sService k8sService = K8sService.get();


  public PodCommand(SimpUserRegistry simpUserRegistry, SimpMessagingTemplate simpMessagingTemplate) {
    this.simpUserRegistry = simpUserRegistry;
    this.simpMessagingTemplate = simpMessagingTemplate;
  }

  private char NEWLINE = '\n';

  private List<String> shlex(CharSequence argString) {
    List<String> tokens = new ArrayList();
    boolean escaping = false;
    char quoteChar = ' ';
    boolean quoting = false;
    StringBuilder current = new StringBuilder();
    for (int i = 0; i < argString.length(); i++) {
      char c = argString.charAt(i);
      if (escaping) {
        current.append(c);
        escaping = false;
      } else if (c == '\\' && !(quoting && quoteChar == '\'')) {
        escaping = true;
      } else if (quoting && c == quoteChar) {
        quoting = false;
      } else if (!quoting && (c == '\'' || c == '"')) {
        quoting = true;
        quoteChar = c;
      } else if (!quoting && Character.isWhitespace(c)) {
        if (current.length() > 0) {
          tokens.add(current.toString());
          current = new StringBuilder();
        }
      } else {
        current.append(c);
      }
    }
    if (current.length() > 0) {
      tokens.add(current.toString());
    }
    return tokens;
  }

  static class ResizePayload{
    int cols;
    int rows;
    String uuid;


    public ResizePayload() {
    }

    public int getCols() {
      return cols;
    }

    public void setCols(int cols) {
      this.cols = cols;
    }

    public int getRows() {
      return rows;
    }

    public void setRows(int rows) {
      this.rows = rows;
    }

    public String getUuid() {
      return uuid;
    }

    public void setUuid(String uuid) {
      this.uuid = uuid;
    }
  }

  public class SimpleListener implements ExecListener{
    @Override
    public void onOpen() {
      Logging.error("Exec session opened");
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
      Logging.error("Exec session failed");
    }

    @Override
    public void onExit(int code, Status status) {
      Logging.error("Exec exited");
    }

    @Override
    public void onClose(int i, String s) {
      Logging.error("Exec session closed");
    }
  }

  public class TerminalSession{
    private ExecWatch execWatch;
    private BlockingDeque<String> blockingDeque;
    private AtomicBoolean waiting = new AtomicBoolean();

    public TerminalSession(ExecWatch execWatch) {
      this.execWatch = execWatch;
      this.blockingDeque = new LinkedBlockingDeque();
      this.waiting.set(true);
    }

    public ExecWatch getExecWatch() {
      return execWatch;
    }

    public void setExecWatch(ExecWatch execWatch) {
      this.execWatch = execWatch;
    }

    public BlockingDeque getBlockingDeque() {
      return blockingDeque;
    }

    public void setBlockingDeque(BlockingDeque blockingDeque) {
      this.blockingDeque = blockingDeque;
    }

    public AtomicBoolean getWaiting() {
      return waiting;
    }

    public void setWaiting(AtomicBoolean waiting) {
      this.waiting = waiting;
    }
  }

  HashMap<String, TerminalSession> commandSessions = new HashMap<>();

  @MessageMapping("/command/session/create")
  @WebSocketUserAuthorized("ws.command.session.create")
  @SendToUser("/client/command/session/create")
  public Object createSession(@Payload String[] connectionDetails, SimpMessageHeaderAccessor headerAccessor, Principal principal) throws ExecutionException {
    String uuid = UUID.randomUUID().toString();

    Logging.error(Base64.getEncoder().encode(new byte[]{CTRL_C}));
    ExecWatch execWatch = k8sService
        .getClient()
        .pods()
        .inNamespace(connectionDetails[0])
        .withName(connectionDetails[1])
        .redirectingInput()
        .redirectingOutput()
        .writingError(System.out)
        .withTTY()
        .usingListener(new SimpleListener())
        .exec();
    TerminalSession terminalSession = new TerminalSession(execWatch);
    commandSessions.put(
        uuid,
        terminalSession
    );
    new Thread(() -> {
      try {
        while (true) {
          if (!terminalSession.getWaiting().get()) {
            if (terminalSession.getBlockingDeque().size() > 0) {
              simpMessagingTemplate.convertAndSend("/client/command/session/send/" + uuid, terminalSession.getBlockingDeque().take());
              terminalSession.getWaiting().set(true);
            }
          }
          Thread.sleep(1L);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }).start();
    new Thread(() -> {
      byte[] buffer = new byte[1024];
      int bytesRead;
      try {
        while (true) {
          bytesRead = execWatch.getOutput().read(buffer);
          terminalSession.getBlockingDeque().add(new String(buffer, 0, bytesRead));
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();
    return uuid;
  }


  @MessageMapping("/command/session/send")
  @WebSocketUserAuthorized("ws.command.session.send")
  public void send(@Payload String[] input, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
    TerminalSession terminalSession = commandSessions.get(input[0]);
    if (terminalSession != null) {
      try {
        terminalSession.getExecWatch().getInput().write((input[1] + NEWLINE).getBytes(StandardCharsets.UTF_8));
        terminalSession.getExecWatch().getInput().flush();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @MessageMapping("/command/session/resize")
  @WebSocketUserAuthorized("ws.command.session.resize")
  public void resize(@Payload ResizePayload resizePayload, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
    TerminalSession terminalSession = commandSessions.get(resizePayload.getUuid());
    if (terminalSession != null) {
      terminalSession.getExecWatch().resize(resizePayload.getCols(), resizePayload.getRows());
    }
  }

  @MessageMapping("/command/session/send_next")
  @WebSocketUserAuthorized("ws.command.session.send_next")
  public void resize(@Payload String uuid, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
    TerminalSession terminalSession = commandSessions.get(uuid);
    if (terminalSession != null) {
      terminalSession.getWaiting().set(false);
    }
  }

  static class TerminalCode{
    private String uuid;
    private char aChar;

    public TerminalCode() {
    }

    public String getUuid() {
      return uuid;
    }

    public void setUuid(String uuid) {
      this.uuid = uuid;
    }

    public char getaChar() {
      return aChar;
    }

    public void setaChar(char aChar) {
      this.aChar = aChar;
    }
  }

  @MessageMapping("/command/session/send_code")
  @WebSocketUserAuthorized("ws.command.session.send_code")
  public void sendCode(@Payload TerminalCode input, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
    TerminalSession terminalSession = commandSessions.get(input.getUuid());
    if (terminalSession == null) {
      return;
    }
    try {
      terminalSession.getExecWatch().getInput().write(input.getaChar());
      terminalSession.getExecWatch().getInput().flush();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @MessageMapping("/command/session/close")
  @WebSocketUserAuthorized("ws.command.session.close")
  @SendToUser("/client/command/session/close")
  public Object closeSession(@Payload String uuid, SimpMessageHeaderAccessor headerAccessor, Principal principalZ) throws ExecutionException {
    TerminalSession terminalSession = commandSessions.get(uuid);
    if (terminalSession == null) {
      return "none";
    }
    terminalSession.getExecWatch().close();
    terminalSession.getBlockingDeque().clear();
    return "closed";
  }


  @PostMapping(value = "/{namespace}/{name}/command", consumes = MediaType.APPLICATION_JSON_VALUE)
  @UserAuthorized("pod.command")
  public void execCommandInPod(@PathVariable("namespace") String namespace,
                               @PathVariable("name") String podName,
                               @RequestBody String[] commands,
                               HttpServletResponse response) throws IOException {
    InputStream outputStream = null;
    for (int i = 0; i < commands.length; i++) {
      String[] command = this.shlex(commands[i]).toArray(new String[0]);
      Logging.error(command);
      try {
        outputStream = k8sService.getClient().pods()
            .inNamespace(namespace)
            .withName(podName)
            .readingInput(System.in)
            .redirectingOutput()
            .writingError(System.err)
            .withTTY()
            .exec(command).getOutput();

        BufferedReader reader = new BufferedReader(new InputStreamReader(outputStream));

        response.setContentType("text/plain");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        PrintWriter out = response.getWriter();
        String line;
        while ((line = reader.readLine()) != null) {
          out.println(line);
        }
        out.flush();
      } catch (Exception ex) {
        throw new RuntimeException("Error executing command in Kubernetes pod", ex);
      } finally {
        if (outputStream != null)
          outputStream.close();
      }

    }
  }
}
