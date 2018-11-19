package io.ap4k.openshift.confg;

import io.ap4k.adapter.KubernetesConfigAdapter;
import io.ap4k.annotation.KubernetesApplication;
import io.ap4k.config.KubernetesConfig;
import io.ap4k.openshift.adapt.OpenshiftConfigAdapter;
import io.ap4k.openshift.annotation.OpenshiftApplication;
import io.ap4k.openshift.config.OpenshiftConfigBuilder;
import io.ap4k.project.ApplyProjectInfo;
import io.ap4k.project.Project;

public class OpenshiftConfigCustomAdapter {

  public static OpenshiftConfigBuilder newBuilder(Project project, OpenshiftApplication openshiftApplication, KubernetesApplication kubernetesApplication) {
    if (openshiftApplication != null) {
      return OpenshiftConfigAdapter.newBuilder(openshiftApplication)
        .accept(new ApplyProjectInfo(project));
    } else if (kubernetesApplication != null) {
      KubernetesConfig kubernetesConfig = KubernetesConfigAdapter.adapt(kubernetesApplication);
      return OpenshiftConfigAdapter.newBuilder(kubernetesConfig)
        .accept(new ApplyProjectInfo(project));
    } else  {
      return new OpenshiftConfigBuilder()
        .accept(new ApplyProjectInfo(project));
    }
  }
}
