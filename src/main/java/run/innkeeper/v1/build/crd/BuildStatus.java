package run.innkeeper.v1.build.crd;

import java.util.List;

public class BuildStatus {
    String jobName;
    List<BuiltContainer> completed;

    BuildState state;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public List<BuiltContainer> getCompleted() {
        return completed;
    }

    public void setCompleted(List<BuiltContainer> completed) {
        this.completed = completed;
    }

    public BuildState getState() {
        return state;
    }

    public void setState(BuildState state) {
        this.state = state;
    }
}
