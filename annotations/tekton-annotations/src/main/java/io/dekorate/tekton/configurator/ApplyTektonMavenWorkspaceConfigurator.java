package io.dekorate.tekton.configurator;

import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.PersistentVolumeClaim;
import io.dekorate.tekton.config.TektonConfigFluent;
import io.dekorate.utils.Strings;

public class ApplyTektonMavenWorkspaceConfigurator extends Configurator<TektonConfigFluent<?>> {

  private final String workspace;
  private final String claim;
  private final PersistentVolumeClaim pvc;

  public ApplyTektonMavenWorkspaceConfigurator(String workspace) {
    this(workspace, null, null);
  }

  public ApplyTektonMavenWorkspaceConfigurator(String workspace, String claim) {
    this(workspace, claim, null);
  }

  public ApplyTektonMavenWorkspaceConfigurator(String workspace, String claim, PersistentVolumeClaim pvc) {
    this.workspace = workspace;
    this.claim = claim;
    this.pvc = pvc;
  }

  @Override
  public void visit(TektonConfigFluent<?> config) {
    if (!Strings.isNotNullOrEmpty(workspace)) {
      config.withM2Workspace(workspace);
    }

    if (!Strings.isNotNullOrEmpty(claim)) {
      config.withExternalM2WorkspaceClaim(claim);
    }

    if (pvc != null) {
      config.withM2WorkspaceClaim(pvc);
    }
  }
}
