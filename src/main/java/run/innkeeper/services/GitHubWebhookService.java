package run.innkeeper.services;

import run.innkeeper.buses.EventBus;
import run.innkeeper.events.actions.builds.WebhookBuildTrigger;
import run.innkeeper.utilities.Logging;
import run.innkeeper.v1.build.crd.Build;
import run.innkeeper.v1.build.crd.BuildState;
import run.innkeeper.v1.build.crd.BuildStatus;
import run.innkeeper.v1.guest.crd.objects.build.GitSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that matches incoming GitHub webhook push events
 * to Build custom resources and triggers builds via the EventBus.
 */
public class GitHubWebhookService {
    private static final GitHubWebhookService singleton = new GitHubWebhookService();

    public static GitHubWebhookService get() {
        return singleton;
    }

    /**
     * Finds all Build CRDs whose git URI matches the pushed repository
     * and triggers builds for them.
     *
     * @param httpsUrl  the HTTPS clone URL from the webhook (e.g. https://github.com/org/repo.git)
     * @param sshUrl    the SSH URL from the webhook (e.g. git@github.com:org/repo.git)
     * @param branch    the branch that was pushed to
     * @param commit    the latest commit SHA after the push
     * @return number of builds triggered
     */
    public int triggerBuildsForRepo(String httpsUrl, String sshUrl, String branch, String commit) {
        K8sService k8sService = K8sService.get();
        List<Build> allBuilds = k8sService.getBuildClient().inAnyNamespace().list().getItems();
        int triggered = 0;

        for (Build build : allBuilds) {
            if (build.getSpec() == null || build.getSpec().getBuildSettings() == null) continue;
            GitSource gitSource = build.getSpec().getBuildSettings().getGit();
            if (gitSource == null || gitSource.getUri() == null) continue;

            String buildUri = gitSource.getUri();

            // Match the Build CRD's git URI against both HTTPS and SSH URLs from webhook
            if (!repoMatches(buildUri, httpsUrl, sshUrl)) continue;

            // If the Build CRD specifies a branch, only trigger for that branch
            if (gitSource.getBranch() != null && !gitSource.getBranch().isEmpty()) {
                if (!gitSource.getBranch().equals(branch)) {
                    Logging.debug("Webhook: skipping build " + build.getMetadata().getName()
                            + " - branch mismatch (wants " + gitSource.getBranch() + ", got " + branch + ")");
                    continue;
                }
            }

            // Check if this commit was already built
            if (build.getStatus() != null && build.getStatus().getCompleted() != null) {
                boolean alreadyBuilt = build.getStatus().getCompleted().stream()
                        .anyMatch(bc -> bc.getGitSource() != null
                                && commit.equals(bc.getGitSource().getCommit()));
                if (alreadyBuilt) {
                    Logging.debug("Webhook: skipping build " + build.getMetadata().getName()
                            + " - commit " + commit + " already built");
                    continue;
                }
            }

            // Only trigger if the build is currently in WAITING state
            if (build.getStatus() != null && build.getStatus().getState() != BuildState.WAITING) {
                Logging.info("Webhook: build " + build.getMetadata().getName()
                        + " is in state " + build.getStatus().getState() + ", skipping webhook trigger");
                continue;
            }

            Logging.info("Webhook: triggering build " + build.getMetadata().getName()
                    + " for repo=" + buildUri + " branch=" + branch + " commit=" + commit);

            // Fire event through EventBus
            EventBus.get().fire(new WebhookBuildTrigger(build, commit, branch, buildUri));
            triggered++;
        }

        if (triggered == 0) {
            Logging.debug("Webhook: no matching Build CRDs found for repo " + httpsUrl);
        }

        return triggered;
    }

    /**
     * Checks if a Build CRD's git URI matches the webhook repo URLs.
     * Handles both SSH (git@github.com:org/repo.git) and HTTPS (https://github.com/org/repo.git) formats.
     */
    private boolean repoMatches(String buildUri, String httpsUrl, String sshUrl) {
        String normalizedBuild = normalizeRepoUrl(buildUri);
        String normalizedHttps = normalizeRepoUrl(httpsUrl);
        String normalizedSsh = normalizeRepoUrl(sshUrl);

        return normalizedBuild.equals(normalizedHttps) || normalizedBuild.equals(normalizedSsh);
    }

    /**
     * Normalizes a git URL to a comparable form: "org/repo"
     * Handles:
     *   git@github.com:org/repo.git -> org/repo
     *   https://github.com/org/repo.git -> org/repo
     *   https://github.com/org/repo -> org/repo
     */
    static String normalizeRepoUrl(String url) {
        if (url == null || url.isEmpty()) return "";

        String normalized = url.trim();

        // Remove trailing .git
        if (normalized.endsWith(".git")) {
            normalized = normalized.substring(0, normalized.length() - 4);
        }

        // Handle SSH format: git@github.com:org/repo
        if (normalized.contains("@") && normalized.contains(":")) {
            int colonIndex = normalized.indexOf(':');
            normalized = normalized.substring(colonIndex + 1);
        }
        // Handle HTTPS format: https://github.com/org/repo
        else if (normalized.startsWith("http")) {
            // Remove protocol and host
            int thirdSlash = normalized.indexOf('/', normalized.indexOf("//") + 2);
            if (thirdSlash >= 0) {
                normalized = normalized.substring(thirdSlash + 1);
            }
        }

        return normalized.toLowerCase();
    }
}
