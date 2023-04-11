package run.innkeeper.v1.build.crd;

public class BuildStatus {
    String jobName;
    BuiltContainer[] completed;

    BuildState state;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public BuiltContainer[] getCompleted() {
        return completed;
    }

    public void setCompleted(BuiltContainer[] completed) {
        this.completed = completed;
    }

    public BuildState getState() {
        return state;
    }

    public void setState(BuildState state) {
        this.state = state;
    }
}
