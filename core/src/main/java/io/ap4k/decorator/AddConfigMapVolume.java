package io.ap4k.decorator;

import io.ap4k.config.ConfigMapVolume;
import io.ap4k.deps.kubernetes.api.model.PodSpecBuilder;

public class AddConfigMapVolume extends Decorator<PodSpecBuilder> {

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
