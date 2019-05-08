
package io.ap4k.testing.kubernetes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import io.ap4k.Ap4kException;
import io.ap4k.kubernetes.config.KubernetesConfig;
import io.ap4k.utils.Serialization;

public interface WithKubernetesConfig {

  String KUBERNETES_CONFIG_PATH = "META-INF/ap4k/.config/kubernetes.yml";

  default boolean hasKubernetesConfig()  {
    return WithKubernetesConfig.class.getClassLoader().getResource(KUBERNETES_CONFIG_PATH) != null;
  }

  default KubernetesConfig getKubernetesConfig() {
    return getKubernetesConfig(KUBERNETES_CONFIG_PATH);
  }

  default KubernetesConfig getKubernetesConfig (String path) {
    URL url = WithKubernetesConfig.class.getClassLoader().getResource(path);
    if (url != null) {
      try (InputStream is = url.openStream())  {
        return Serialization.unmarshal(is, KubernetesConfig.class);
      } catch (IOException e) {
        throw Ap4kException.launderThrowable(e);
      }
    }
    throw new IllegalStateException("Expected to find kubernetes config at: "+path+"!");
  }
}
