package io.ap4k.visitor;

import io.ap4k.config.AzureDiskVolume;
import io.ap4k.config.AzureFileVolume;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;

public class AddAzureFileVolume extends TypedVisitor<PodSpecBuilder> {

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
