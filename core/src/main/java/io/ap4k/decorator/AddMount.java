package io.ap4k.decorator;

import io.ap4k.config.Mount;
import io.ap4k.deps.kubernetes.api.model.ContainerBuilder;

public class AddMount extends Decorator<ContainerBuilder> {

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
