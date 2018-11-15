package io.ap4k.visitor;

import io.ap4k.config.ConfigMapVolume;
import io.ap4k.config.SecretVolume;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;

public class AddConfigMapVolume extends TypedVisitor<PodSpecBuilder> {

    private final ConfigMapVolume volume;

    public AddConfigMapVolume(ConfigMapVolume volume) {
        this.volume = volume;
    }

    @Override
    public void visit(PodSpecBuilder podSpec) {
        podSpec.addNewVolume()
                .withName(volume.getVolumeName())
                .withNewConfigMap()
                    .withName(volume.getConfigMapName())
                    .withDefaultMode(volume.getDefaultMode())
                    .withOptional(volume.isOptional())
                .endConfigMap();

    }
}
