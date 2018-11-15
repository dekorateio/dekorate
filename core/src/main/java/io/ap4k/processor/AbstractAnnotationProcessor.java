/**
 * Copyright (C) 2018 Ioannis Canellos 
 *     
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
**/
package io.ap4k.processor;

import io.ap4k.Session;
import io.ap4k.adapter.KubernetesConfigAdapter;
import io.ap4k.annotation.KubernetesApplication;
import io.ap4k.config.Configuration;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.config.KubernetesConfig;
import io.ap4k.project.ApplyProjectInfo;
import io.ap4k.project.Project;
import io.ap4k.project.ProjectFactory;
import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.utils.Serialization;

import javax.annotation.processing.AbstractProcessor;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public abstract class AbstractAnnotationProcessor<C extends Configuration> extends AbstractProcessor {

    protected static final String PACKAGE = "";
    protected static final String FILENAME = "META-INF/ap4k/%s.%s";
    protected static final String JSON = "json";
    protected static final String YML = "yml";

    /**
     * Get or create a new config for the specified {@link Element}.
     * @param mainClass     The type element of the annotated class (Main).
     * @return              A new config.
     */
    public ConfigurationSupplier<C> configuration(Element mainClass) {
        KubernetesApplication application = mainClass.getAnnotation(KubernetesApplication.class);
        Project project = ProjectFactory.create(processingEnv);

        return new ConfigurationSupplier<C>((VisitableBuilder<C, ?>) KubernetesConfigAdapter
                .newBuilder(application)
                .accept(new ApplyProjectInfo(project)));
    }

    /**
     * Writes all {@link Session} resources.
     * @param session The target session.
     */
    protected void write(Session session) {
        Map<String, KubernetesList> resources = session.close();
        resources.forEach((g, l) -> write(g, l));
    }


    /**
     * Writes all {@link Session} resources.
     * @param resources The target session resources.
     */
    protected void write(Map<String, KubernetesList> resources) {
        resources.forEach((g, l) -> write(g, l));
    }

    /**
     * Write the resources contained in the {@link KubernetesList} in a directory named after the specififed group.
     * @param group The group.
     * @param list  The resource list.
     */
    protected void write(String group, KubernetesList list) {
        try {
            FileObject json = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, PACKAGE, String.format(FILENAME, group, JSON));
            try (Writer writer = json.openWriter()) {
                writer.write(Serialization.asJson(list));
            }
            FileObject yml = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, PACKAGE, String.format(FILENAME, group, YML));
            try (Writer writer = yml.openWriter()) {
                writer.write(Serialization.asYaml(list));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing resources");
        }
    }
}
