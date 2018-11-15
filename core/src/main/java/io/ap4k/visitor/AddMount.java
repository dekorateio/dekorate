package io.ap4k.visitor;

import io.ap4k.config.Mount;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.ContainerBuilder;

public class AddMount extends TypedVisitor<ContainerBuilder> {

    private final Mount mount;

    public AddMount(Mount mount) {
        this.mount = mount;
    }

    @Override
    public void visit(ContainerBuilder container) {
        container.addNewVolumeMount()
                .withName(mount.getName())
                .withMountPath(mount.getPath())
                .withSubPath(mount.getSubPath())
                .withReadOnly(mount.isReadOnly())
                .endVolumeMount();


    }
}
