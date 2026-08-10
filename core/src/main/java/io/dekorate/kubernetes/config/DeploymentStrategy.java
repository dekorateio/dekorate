package io.dekorate.kubernetes.config;

public enum DeploymentStrategy {

  None, // This is added to imply that no explicit setting has been provided
  Recreate, RollingUpdate
}
