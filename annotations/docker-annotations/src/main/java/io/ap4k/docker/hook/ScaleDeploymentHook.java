package io.ap4k.docker.hook;

import io.ap4k.hook.ProjectHook;
import io.ap4k.project.Project;

public class ScaleDeploymentHook extends ProjectHook {

    private final String name;
    private final int replicas;

    public ScaleDeploymentHook(Project project, String name, int replicas) {
        super(project);
        this.name = name;
        this.replicas = replicas;
    }

    @Override
    public void init() {

    }

    @Override
    public void warmup() {

    }

    @Override
    public void run() {
        exec("kubectl", "scale", "deployment/" + name, "--replicas="+replicas);
    }
}
