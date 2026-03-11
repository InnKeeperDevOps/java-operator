package run.innkeeper.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import run.innkeeper.api.services.AdminUIFileSystem;
import run.innkeeper.services.GitHubWebhookService;
import run.innkeeper.utilities.Logging;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * REST endpoint that receives GitHub webhook push events.
 * This replaces the need for constant Git polling by reacting
 * to pushes in real-time.
 *
 * Configure in GitHub repo Settings > Webhooks:
 *   - Payload URL: https://your-operator-host:8081/webhook/github
 *   - Content type: application/json
 *   - Secret: (set GITHUB_WEBHOOK_SECRET env var to match)
 *   - Events: Just the push event
 */
@RestController
@RequestMapping("/webhook")
public class GitHubWebhookController {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ADMIN_UI_REPO = "InnKeeperDevOps/admin-ui";
    private final GitHubWebhookService webhookService = GitHubWebhookService.get();

    @Autowired
    private AdminUIFileSystem adminUIFileSystem;

    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signatureHeader,
            @RequestHeader(value = "X-GitHub-Event", required = false) String eventType,
            @RequestBody String payload) {

        String secret = System.getenv("GITHUB_WEBHOOK_SECRET");

        // Verify signature if a secret is configured
        if (secret != null && !secret.isEmpty()) {
            if (signatureHeader == null || !verifySignature(payload, signatureHeader, secret)) {
                Logging.error("GitHub webhook signature verification failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }
        }

        // Only process push events
        if (!"push".equals(eventType)) {
            Logging.debug("Ignoring GitHub webhook event type: " + eventType);
            return ResponseEntity.ok("Event type ignored: " + eventType);
        }

        try {
            JsonNode json = objectMapper.readTree(payload);

            String repoUrl = json.path("repository").path("clone_url").asText("");
            String sshUrl = json.path("repository").path("ssh_url").asText("");
            String ref = json.path("ref").asText(""); // e.g. "refs/heads/main"
            String afterCommit = json.path("after").asText("");
            String repoFullName = json.path("repository").path("full_name").asText("");

            // Extract branch name from ref (refs/heads/main -> main)
            String branch = ref.startsWith("refs/heads/") ? ref.substring("refs/heads/".length()) : ref;

            Logging.info("GitHub webhook received: push to " + repoFullName + " branch=" + branch + " commit=" + afterCommit);

            // Handle admin-ui repo separately
            if (ADMIN_UI_REPO.equalsIgnoreCase(repoFullName)) {
                adminUIFileSystem.refreshFromWebhook(afterCommit);
            }

            // Match against Build CRDs and trigger builds
            int triggered = webhookService.triggerBuildsForRepo(repoUrl, sshUrl, branch, afterCommit);

            return ResponseEntity.ok("Triggered " + triggered + " build(s) for " + repoFullName);

        } catch (Exception e) {
            Logging.error("Failed to process GitHub webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Processing failed");
        }
    }

    /**
     * Verifies the HMAC-SHA256 signature from GitHub.
     */
    private boolean verifySignature(String payload, String signatureHeader, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder("sha256=");
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return MessageDigest.isEqual(
                    hexString.toString().getBytes(StandardCharsets.UTF_8),
                    signatureHeader.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            Logging.error("Webhook signature verification error: " + e.getMessage());
            return false;
        }
    }
}
