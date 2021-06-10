package io.dekorate.tekton.configurator;

import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.PersistentVolumeClaim;
import io.dekorate.tekton.config.TektonConfigFluent;
import io.dekorate.utils.Strings;

public class ApplyTektonSourceWorkspaceConfigurator extends Configurator<TektonConfigFluent<?>> {

  private final String workspace;
  private final String claim;
  private final PersistentVolumeClaim pvc;

  public ApplyTektonSourceWorkspaceConfigurator(String workspace) {
    this(workspace, null, null);
  }

  public ApplyTektonSourceWorkspaceConfigurator(String workspace, String claim) {
    this(workspace, claim, null);
  }

  public ApplyTektonSourceWorkspaceConfigurator(String workspace, String claim, PersistentVolumeClaim pvc) {
    this.workspace = workspace;
    this.claim = claim;
    this.pvc = pvc;
  }

  @Override
  public void visit(TektonConfigFluent<?> config) {
    if (!Strings.isNotNullOrEmpty(workspace)) {
      config.withSourceWorkspace(workspace);
    }

    if (!Strings.isNotNullOrEmpty(claim)) {
      config.withExternalSourceWorkspaceClaim(claim);
    }

    if (pvc != null) {
      config.withSourceWorkspaceClaim(pvc);
    }
  }
}
