package io.ap4k.testing;

import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

public class Testing {

  /**
   * This is an {@link org.junit.jupiter.api.extension.ExtensionContext.Namespace} that is used to store all ap4k
   * related state.
   * Note: Not to be confused with KubernetesExtension namespaces, this is a junit5 construct.
   */
  public static Namespace AP4K_STORE = Namespace.create("AP4K_STORE");





}
