package run.innkeeper.events.actions.builds;

import run.innkeeper.events.structure.BuildEvent;
import run.innkeeper.v1.build.crd.Build;

/**
 * Event fired when a GitHub webhook push event is received,
 * indicating that a build should be triggered immediately
 * without polling Git for changes.
 */
public class WebhookBuildTrigger extends BuildEvent {
    private final String commit;
    private final String branch;
    private final String repoUrl;

    public WebhookBuildTrigger(Build build, String commit, String branch, String repoUrl) {
        super(build);
        this.commit = commit;
        this.branch = branch;
        this.repoUrl = repoUrl;
    }

    public String getCommit() {
        return commit;
    }

    public String getBranch() {
        return branch;
    }

    public String getRepoUrl() {
        return repoUrl;
    }
}
