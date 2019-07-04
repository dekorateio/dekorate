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
 */
package io.dekorate.application.handler;

import io.dekorate.Handler;
import io.dekorate.Resources;
import io.dekorate.application.config.ApplicationConfig;
import io.dekorate.application.config.Contact;
import io.dekorate.application.config.EditableApplicationConfig;
import io.dekorate.application.config.Icon;
import io.dekorate.application.decorator.GroupKindsDecorator;
import io.dekorate.deps.applicationcrd.api.model.ApplicationBuilder;
import io.dekorate.deps.applicationcrd.api.model.ContactData;
import io.dekorate.deps.applicationcrd.api.model.ContactDataBuilder;
import io.dekorate.deps.applicationcrd.api.model.ImageSpec;
import io.dekorate.deps.applicationcrd.api.model.ImageSpecBuilder;
import io.dekorate.deps.applicationcrd.api.model.Link;
import io.dekorate.deps.applicationcrd.api.model.LinkBuilder;
import io.dekorate.kubernetes.config.Configuration;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ApplicationHandler implements Handler<ApplicationConfig> {

  private final Resources resources;

  public ApplicationHandler(Resources resources) {
    this.resources = resources;
  }

  @Override
  public int order() {
    return 10000; //It is esential that this will run last.
  }

  @Override
  public void handle(ApplicationConfig config) {
    resources.add(new ApplicationBuilder()
      .withNewMetadata()
        .withName(resources.getName())
        .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec()
      .withNewSelector()
        .withMatchLabels(resources.getLabels())
      .endSelector()
      .withNewDescriptor()
        .withVersion(resources.getVersion())
        .withOwners(Arrays.stream(config.getOwners()).map(ApplicationHandler::adapt).collect(Collectors.toList()))
        .withMaintainers(Arrays.stream(config.getMaintainers()).map(ApplicationHandler::adapt).collect(Collectors.toList()))
        .withIcons(Arrays.stream(config.getIcons()).map(ApplicationHandler::adapt).collect(Collectors.toList()))
        .withLinks(Arrays.stream(config.getLinks()).map(ApplicationHandler::adapt).collect(Collectors.toList()))
        .withKeywords(config.getKeywords())
        .withNotes(config.getNotes())
      .endDescriptor()
      .endSpec()
    .build());

    resources.decorate(new GroupKindsDecorator());
  }

  @Override
  public boolean canHandle(Class<? extends Configuration> config) {
    return ApplicationConfig.class.equals(config) || EditableApplicationConfig.class.equals(config);
  }

  private static ContactData adapt(Contact contact) {
    return new ContactDataBuilder()
      .withName(contact.getName())
      .withEmail(contact.getEmail())
      .withUrl(contact.getUrl())
      .build();
  }

  private static Link adapt(io.dekorate.application.config.Link link) {
    return new LinkBuilder()
      .withUrl(link.getUrl())
      .withDescription(link.getDescription())
      .build();
  }

  private static ImageSpec adapt(Icon icon) {
    return new ImageSpecBuilder()
      .withSize(icon.getSize())
      .withSrc(icon.getSrc())
      .withType(icon.getType())
      .build();
  }
}
