package io.dekorate.kubernetes.config;

public enum DeploymentStrategy {

  None, //This is added to imply that no explict setting has been provided
  Recreate, RollingUpdate
}
