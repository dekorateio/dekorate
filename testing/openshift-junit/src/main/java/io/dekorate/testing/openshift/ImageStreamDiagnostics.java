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

package io.dekorate.testing.openshift;

import io.dekorate.testing.AbstractDiagonsticsService;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.client.OpenShiftClient;

public class ImageStreamDiagnostics extends AbstractDiagonsticsService<ImageStream> {

  public ImageStreamDiagnostics(KubernetesClient client) {
    super(client);
  }

  @Override
  public void display(ImageStream imageStream) {
    LOGGER.info("Diagnostics for kind: [ImageStream] with name : [" + imageStream.getMetadata().getName() + "].");
    displayStatus(imageStream);
    displayEvents(imageStream);
  }

  @Override
  public void displayStatus(ImageStream imageStream) {
    String name = imageStream.getMetadata().getName();
    ImageStream updated = getKubernetesClient().adapt(OpenShiftClient.class).imageStreams().withName(name).get();
    LOGGER.info("Spec:");
    LOGGER.info("\tDocker image repository:" + imageStream.getSpec().getDockerImageRepository());
    LOGGER.info("\tLookup policy:" + imageStream.getSpec().getLookupPolicy());
    if (updated != null) {
      LOGGER.info("Status:");
      LOGGER.info("\tDocker image repository:" + updated.getStatus().getDockerImageRepository());
      LOGGER.info("\tPublic docker image repository:" + updated.getStatus().getPublicDockerImageRepository());
      LOGGER.info("\tTags:");
      if (updated.getStatus().getTags() != null && !updated.getStatus().getTags().isEmpty()) {
        updated.getStatus().getTags().stream().peek(t -> LOGGER.info("\t" + t));
      } else {
        LOGGER.warning("\t\tNo tags found for ImageStream: [" + name + "]");
      }
    } else {
      LOGGER.error("Failed to retrieve ImageStream: [" + name + "]");
    }
  }
}
