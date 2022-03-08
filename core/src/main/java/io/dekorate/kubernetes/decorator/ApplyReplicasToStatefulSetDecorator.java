package io.dekorate.kubernetes.decorator;

import static io.dekorate.ConfigReference.generateConfigReferenceName;

import java.util.Arrays;
import java.util.List;

import io.dekorate.ConfigReference;
import io.dekorate.WithConfigReferences;
import io.dekorate.doc.Description;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.StatefulSetSpecFluent;

@Description("Apply the number of replicas to the StatefulSetSpec.")
public class ApplyReplicasToStatefulSetDecorator extends NamedResourceDecorator<StatefulSetSpecFluent>
    implements WithConfigReferences {
  private final int replicas;

  public ApplyReplicasToStatefulSetDecorator(int replicas) {
    this(ANY, replicas);
  }

  public ApplyReplicasToStatefulSetDecorator(String statefulSetName, int replicas) {
    super(statefulSetName);
    this.replicas = replicas;
  }

  public void andThenVisit(StatefulSetSpecFluent statefulSetSpec, ObjectMeta resourceMeta) {
    if (this.replicas > 0) {
      statefulSetSpec.withReplicas(this.replicas);
    }
  }

  @Override
  public List<ConfigReference> getConfigReferences() {
    return Arrays.asList(buildConfigReferenceReplicas());
  }

  private ConfigReference buildConfigReferenceReplicas() {
    String property = generateConfigReferenceName("replicas", getName());
    String jsonPath = "$.[?(@.kind == 'StatefulSet')].spec.replicas";
    if (!Strings.equals(getName(), ANY)) {
      jsonPath = "$.[?(@.kind == 'StatefulSet' && @.metadata.name == '" + getName() + "')].spec.replicas";
    }

    return new ConfigReference(property, jsonPath);
  }
}
