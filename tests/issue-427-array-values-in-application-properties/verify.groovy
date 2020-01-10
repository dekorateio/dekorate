import io.dekorate.utils.Serialization
import io.dekorate.deps.kubernetes.api.model.KubernetesList
import io.dekorate.deps.kubernetes.api.model.apps.Deployment;

//Check that file exits
String base = basedir
File kubernetesYml = new File(base, "target/classes/META-INF/dekorate/kubernetes.yml")
assert !kubernetesYml.exists()
