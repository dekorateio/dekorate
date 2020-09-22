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

package io.dekorate.application.decorator;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.dekorate.application.config.ApplicationConfig;
import io.dekorate.application.config.Contact;
import io.dekorate.application.config.Icon;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.utils.Labels;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import me.snowdrop.applicationcrd.api.model.ApplicationBuilder;
import me.snowdrop.applicationcrd.api.model.ContactData;
import me.snowdrop.applicationcrd.api.model.ContactDataBuilder;
import me.snowdrop.applicationcrd.api.model.ImageSpec;
import me.snowdrop.applicationcrd.api.model.ImageSpecBuilder;
import me.snowdrop.applicationcrd.api.model.Link;
import me.snowdrop.applicationcrd.api.model.LinkBuilder;

@Description("Add a Application resource to the list of generated resources.")
public class AddApplicationResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private ApplicationConfig config;

  public AddApplicationResourceDecorator(ApplicationConfig config) {
    this.config = config;
  }

  public void visit(KubernetesListBuilder list) {
    ObjectMeta meta = getMandatoryDeploymentMetadata(list);
    list.addToItems(new ApplicationBuilder()
        .withNewMetadata()
        .withName(meta.getName())
        .withLabels(meta.getLabels())
        .endMetadata()
        .withNewSpec()
        .withNewSelector()
        .withMatchLabels(meta.getLabels())
        .endSelector()
        .withNewDescriptor()
        .withVersion(meta.getLabels().getOrDefault(Labels.VERSION, "latest"))
        .withOwners(Arrays.stream(config.getOwners()).map(AddApplicationResourceDecorator::adapt)
            .collect(Collectors.toList()))
        .withLinks(Arrays.stream(config.getLinks()).map(AddApplicationResourceDecorator::adapt)
            .collect(Collectors.toList()))
        .withMaintainers(Arrays.stream(config.getMaintainers()).map(AddApplicationResourceDecorator::adapt)
            .collect(Collectors.toList()))
        .withIcons(Arrays.stream(config.getIcons()).map(AddApplicationResourceDecorator::adapt)
            .collect(Collectors.toList()))
        .withKeywords(config.getKeywords())
        .withNotes(config.getNotes())
        .endDescriptor()
        .endSpec());
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
