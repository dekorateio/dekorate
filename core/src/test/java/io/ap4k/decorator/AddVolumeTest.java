package io.ap4k.decorator;

import io.ap4k.deps.kubernetes.api.model.Pod;
import io.ap4k.deps.kubernetes.api.model.PodBuilder;
import org.junit.jupiter.api.Test;

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
