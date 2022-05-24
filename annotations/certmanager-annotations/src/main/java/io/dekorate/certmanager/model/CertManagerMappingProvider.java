package io.dekorate.certmanager.model;

import java.util.HashMap;
import java.util.Map;

import io.fabric8.certmanager.api.model.v1.Certificate;
import io.fabric8.certmanager.api.model.v1.Issuer;
import io.fabric8.kubernetes.api.KubernetesResourceMappingProvider;
import io.fabric8.kubernetes.api.model.KubernetesResource;

public class CertManagerMappingProvider implements KubernetesResourceMappingProvider {

  private Map<String, Class<? extends KubernetesResource>> mappings = new HashMap<String, Class<? extends KubernetesResource>>() {
    {
      put("cert-manager.io/v1#Certificate", Certificate.class);
      put("cert-manager.io/v1#Issuer", Issuer.class);
    }
  };

  public Map<String, Class<? extends KubernetesResource>> getMappings() {
    return mappings;
  }
}
