package run.innkeeper.buses;



public class BuildBus extends JobBus {
    private static BuildBus bus = new BuildBus();

    public BuildBus() {
        super("build", "ghcr.io/innkeeperdevops/git-builder:28");
    }

    public static BuildBus get(){
        return bus;
    }

}
