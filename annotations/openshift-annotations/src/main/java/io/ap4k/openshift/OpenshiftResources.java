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
package io.ap4k.openshift;

import io.ap4k.openshift.config.OpenshiftConfig;
import io.ap4k.openshift.config.SourceToImageConfig;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.BuildConfigBuilder;
import io.fabric8.openshift.api.model.ImageStreamBuilder;
import io.fabric8.openshift.api.model.ImageStream;
import io.ap4k.utils.Images;

import java.util.HashMap;
import java.util.Map;

import static io.ap4k.openshift.Constants.DEFAULT_SOURCE_TO_IMAGE_CONFIG;
import static io.ap4k.openshift.Constants.SOURCE_TO_IMAGE_CONFIG;

public class OpenshiftResources {

    private static final String APP = "app";
    private static final String VERSION = "version";

    private static final String IF_NOT_PRESENT = "IfNotPresent";
    private static final String KUBERNETES_NAMESPACE = "KUBERNETES_NAMESPACE";
    private static final String METADATA_NAMESPACE = "metadata.namespace";

    private static final String IMAGESTREAMTAG = "ImageStreamTag";
    private static final String IMAGECHANGE = "ImageChange";

    private static final String JAVA_APP_JAR = "JAVA_APP_JAR";

    /**
     * Creates a {@link DeploymentConfig} for the {@link OpenshiftConfig}.
     * @param config   The sesssion.
     * @return          The deployment config.
     */
    public static DeploymentConfig createDeploymentConfig(OpenshiftConfig config)  {
        SourceToImageConfig sourceToImageConfig = config.getAttributeOrDefault(SOURCE_TO_IMAGE_CONFIG);
        String repository = Images.getRepository(sourceToImageConfig.getBuilderImage());
        String tag = Images.getTag(sourceToImageConfig.getBuilderImage());

        return new DeploymentConfigBuilder()
            .withNewMetadata()
            .withName(config.getName())
            .endMetadata()
            .withNewSpec()
            .withNewReplicas(1)
            .withTemplate(createPodTemplateSpec(config))
            .withSelector(createLabels(config))
            .addNewTrigger()
            .withType(IMAGECHANGE)
            .withNewImageChangeParams()
            .withAutomatic(true)
            .withContainerNames(config.getName())
            .withNewFrom()
            .withKind(IMAGESTREAMTAG)
            .withName(config.getName() + ":" + config.getVersion())
            .endFrom()
            .endImageChangeParams()
            .endTrigger()
            .endSpec()
            .build();
    }

    /**
     * Creates a {@link Map} with the labels for the {@link OpenshiftConfig}.
     * @param config   The config.
     * @return          A map containing the labels.
     */
    public static Map<String, String> createLabels(OpenshiftConfig config) {
        return new HashMap<String, String >() {{
            put(APP, config.getName());
            put(VERSION, config.getVersion());
        }};
    }


    /**
     * Creates a {@link PodTemplateSpec} for the {@link OpenshiftConfig}.
     * @param config   The sesssion.
     * @return          The pod template specification.
     */
    public static PodTemplateSpec createPodTemplateSpec(OpenshiftConfig config) {
        return new PodTemplateSpecBuilder()
            .withSpec(createPodSpec(config))
            .withNewMetadata()
            .withLabels(createLabels(config))
            .endMetadata()
            .build();
    }

    /**
     * Creates a {@link PodSpec} for the {@link OpenshiftConfig}.
     * @param config   The sesssion.
     * @return          The pod specification.
     */
    public static PodSpec createPodSpec(OpenshiftConfig config) {
        return new PodSpecBuilder()
            .addNewContainer()
            .withName(config.getName())
            .withImage("")
            .withImagePullPolicy(IF_NOT_PRESENT)
            .addNewEnv()
            .withName(KUBERNETES_NAMESPACE)
            .withNewValueFrom()
            .withNewFieldRef(null, METADATA_NAMESPACE)
            .endValueFrom()
            .endEnv()
            .addNewEnv()
            .withName(JAVA_APP_JAR)
            .withValue(config.getProject().getBuildInfo().getOutputFileName())
            .endEnv()
            .endContainer()
            .build();
    }


    /**
     * Create an {@link ImageStream} for the {@link OpenshiftConfig}.
     * @param config   The config.
     * @return         The build config.
     */
    public static ImageStream createBuilderImageStream(OpenshiftConfig config) {
        SourceToImageConfig sourceToImageConfig = config.getAttributeOrDefault(SOURCE_TO_IMAGE_CONFIG);
        String repository = Images.getRepository(sourceToImageConfig.getBuilderImage());

        String name = !repository.contains("/")
            ? repository
            : repository.substring(repository.lastIndexOf("/") + 1);

        return new ImageStreamBuilder()
            .withNewMetadata()
            .withName(name)
            .endMetadata()
            .withNewSpec()
            .withDockerImageRepository(repository)
            .endSpec()
            .build();
    }

    
    /**
     * Create an {@link ImageStream} for the {@link OpenshiftConfig}.
     * @param config   The config.
     * @return         The build config.
     */
    public static ImageStream createProjectImageStream(OpenshiftConfig config) {
        return new ImageStreamBuilder()
            .withNewMetadata()
            .withName(config.getName())
            .endMetadata()
            .build();
    }
    
    /**
     * Create a {@link BuildConfig} for the {@link OpenshiftConfig}.
     * @param config   The config.
     * @return          The build config.
     */
    public static BuildConfig createBuildConfig(OpenshiftConfig config) {
        SourceToImageConfig sourceToImageConfig = config.getAttributeOrDefault(SOURCE_TO_IMAGE_CONFIG);
        String builderRepository = Images.getRepository(sourceToImageConfig.getBuilderImage());
        String builderTag = Images.getTag(sourceToImageConfig.getBuilderImage());

        String builderName = !builderRepository.contains("/")
            ? builderRepository
            : builderRepository.substring(builderRepository.lastIndexOf("/") + 1);


        return new BuildConfigBuilder()
            .withNewMetadata()
            .withName(config.getName())
            .endMetadata()
            .withNewSpec()
            .withNewOutput()
            .withNewTo()
            .withKind(IMAGESTREAMTAG)
            .withName(config.getName() + ":" + config.getVersion())
            .endTo()
            .endOutput()
            .withNewSource()
            .withNewBinary()
            .endBinary()
            .endSource()
            .withNewStrategy()
            .withNewSourceStrategy()
            .withNewFrom()
            .withKind(IMAGESTREAMTAG)
            .withName(builderName + ":" + builderTag)
            .endFrom()
            .endSourceStrategy()
            .endStrategy()
            .endSpec()
            .build();
    }
}
