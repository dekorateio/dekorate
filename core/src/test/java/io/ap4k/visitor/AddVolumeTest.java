package io.ap4k.visitor;

import io.ap4k.config.Annotation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class AddVolumeTest {

    @Test
    public void shouldAddAnnotationToResources() {
        Pod expectecd = new PodBuilder()
                .withNewMetadata()
                .withName("pod")
                .endMetadata()
                .withNewSpec()
                .addNewVolume()
                .withNewAwsElasticBlockStore()
                .endAwsElasticBlockStore()
                .endVolume()
                .endSpec()
                .build();

    }
}