package io.dekorate.core;

import io.fabric8.kubernetes.api.model.HasMetadata;

public interface Generator {

  String GROUP_COMMON = "common";
  String GROUP_DEPLOYMENT = "deployment";
  String GROUP_SERVICE = "service";
  String GROUP_SERVICE_ACCOUNT = "serviceaccount";
  String GROUP_RBAC = "rbac";
  String GROUP_SECRET = "secret";

  String getGroup();

  HasMetadata generate(Configuration config, VisitorRegistry registry);
}
