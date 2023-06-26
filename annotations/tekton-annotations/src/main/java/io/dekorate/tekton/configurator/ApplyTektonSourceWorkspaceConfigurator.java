package io.dekorate.tekton.configurator;

import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.PersistentVolumeClaim;
import io.dekorate.tekton.config.TektonConfigFluent;
import io.dekorate.utils.Strings;

public class ApplyTektonSourceWorkspaceConfigurator extends Configurator<TektonConfigFluent<?>> {

  private final String workspace;
  private final PersistentVolumeClaim pvc;

  public ApplyTektonSourceWorkspaceConfigurator(String workspace) {
    this(workspace, null);
  }

  public ApplyTektonSourceWorkspaceConfigurator(String workspace, PersistentVolumeClaim pvc) {
    this.workspace = workspace;
    this.pvc = pvc;
  }

  @Override
  public void visit(TektonConfigFluent<?> config) {
    if (!Strings.isNotNullOrEmpty(workspace)) {
      config.withSourceWorkspace(workspace);
    }

    if (pvc != null) {
      config.withSourceWorkspaceClaim(pvc);
    }
  }
}
