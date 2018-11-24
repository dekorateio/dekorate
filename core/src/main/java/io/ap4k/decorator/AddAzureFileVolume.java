package io.ap4k.decorator;

import io.ap4k.config.AzureFileVolume;
import io.ap4k.deps.kubernetes.api.model.PodSpecBuilder;

public class AddAzureFileVolume extends Decorator<PodSpecBuilder> {

    private final AzureFileVolume volume;

    public AddAzureFileVolume(AzureFileVolume volume) {
        this.volume = volume;
    }

    @Override
    public void visit(PodSpecBuilder podSpec) {
        podSpec.addNewVolume()
                .withName(volume.getVolumeName())
                .withNewAzureFile()
                .withSecretName(volume.getSecretName())
                .withShareName(volume.getShareName())
                .withReadOnly(volume.isReadOnly())
                .endAzureFile();

    }
}
