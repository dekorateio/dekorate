package io.ap4k.project;

import io.ap4k.config.Configurator;
import io.ap4k.config.KubernetesConfigFluent;
import io.ap4k.utils.Strings;

public class ApplyProjectInfo extends Configurator<KubernetesConfigFluent> {

    private static final String APP_GROUP = "app.group";
    private static final String APP_NAME = "app.name";
    private static final String APP_VERSION = "app.version";

    private static final String DEFAULT_GROUP = "default";

    private final Project project;

    public ApplyProjectInfo(Project project) {
        this.project = project;
    }

    @Override
    public void visit(KubernetesConfigFluent fluent) {
        fluent.withProject(project);
        fluent.withGroup(System.getProperty(APP_GROUP, Strings.isNotNullOrEmpty(fluent.getName()) ? fluent.getName() : DEFAULT_GROUP))
                .withName(System.getProperty(APP_NAME, Strings.isNotNullOrEmpty(fluent.getName()) ? fluent.getName() : project.getBuildInfo().getName()))
                .withVersion(System.getProperty(APP_VERSION, Strings.isNotNullOrEmpty(fluent.getVersion()) ? fluent.getVersion() : project.getBuildInfo().getVersion()));
    }
}
