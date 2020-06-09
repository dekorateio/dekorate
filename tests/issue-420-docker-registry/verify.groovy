import io.dekorate.utils.Serialization
import io.fabric8.kubernetes.api.model.KubernetesList
import io.fabric8.kubernetes.api.model.apps.Deployment;

//Check that file exits
String base = basedir
File kubernetesYml = new File(base, "target/classes/META-INF/dekorate/kubernetes.yml")
assert !kubernetesYml.exists()
