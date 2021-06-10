/**
 * Copyright 2018 The original authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
**/

package io.dekorate.tekton.step;

public interface ImageStep extends Step {

  String PATH_TO_DOCKERFILE_PARAM_NAME = "pathToDockerfile";
  String PATH_TO_DOCKERFILE_PARAM_DESCRIPTION = "Path to Dockerfile";
  String PATH_TO_DOCKERFILE_PARAM_DEFAULT = "Dockerfile";

  String DOCKER_SOCKET_NAME = "docker-socket";
  String DOCKER_SOCKET_PATH = "/var/run/docker.sock";
  String DOCKER_SOCKET_TYPE = "Socket";
  boolean DOCKER_SOCKET_READ_ONLY = false;
}
