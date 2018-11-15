package io.ap4k.visitor;

import io.ap4k.config.AzureDiskVolume;
import io.ap4k.config.ConfigMapVolume;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;

public class AddAzureDiskVolume extends TypedVisitor<PodSpecBuilder> {

    private final AzureDiskVolume volume;

    public AddAzureDiskVolume(AzureDiskVolume volume) {
        this.volume = volume;
    }

    @Override
    public void visit(PodSpecBuilder podSpec) {
        podSpec.addNewVolume()
                .withName(volume.getVolumeName())
                .withNewAzureDisk()
                .withKind(volume.getKind())
                .withDiskName(volume.getDiskName())
                .withDiskURI(volume.getDiskURI())
                .withFsType(volume.getFsType())
                .withCachingMode(volume.getCachingMode())
                .withReadOnly(volume.isReadOnly())
                .endAzureDisk();

    }
}
