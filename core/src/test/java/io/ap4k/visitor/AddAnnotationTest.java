package io.ap4k.visitor;

import io.ap4k.config.Annotation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddAnnotationTest {

    @Test
    public void shouldAddAnnotationToResources() {
        Pod expectecd = new PodBuilder()
                .withNewMetadata()
                .withName("pod")
                .addToAnnotations("key1","value1")
                .endMetadata()
                .build();

        Pod actual = new PodBuilder()
                .withNewMetadata()
                .withName("pod")
                .endMetadata()
                .accept(new AddAnnotation(new Annotation("key1", "value1")))
                .build();

        assertEquals(expectecd, actual);
    }
}
