package io.dekorate.tekton.configurator;

import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.PersistentVolumeClaim;
import io.dekorate.tekton.config.TektonConfigFluent;
import io.dekorate.utils.Strings;

public class ApplyTektonMavenWorkspaceConfigurator extends Configurator<TektonConfigFluent<?>> {

  private final String workspace;
  private final PersistentVolumeClaim pvc;

  public ApplyTektonMavenWorkspaceConfigurator(String workspace) {
    this(workspace, null);
  }

  public ApplyTektonMavenWorkspaceConfigurator(String workspace, PersistentVolumeClaim pvc) {
    this.workspace = workspace;
    this.pvc = pvc;
  }

  @Override
  public void visit(TektonConfigFluent<?> config) {
    if (!Strings.isNotNullOrEmpty(workspace)) {
      config.withM2Workspace(workspace);
    }

    if (pvc != null) {
      config.withM2WorkspaceClaim(pvc);
    }
  }
}
