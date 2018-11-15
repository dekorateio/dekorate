/**
 * Copyright (C) 2018 Ioannis Canellos 
 *     
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
**/
package io.ap4k.visitor;

import io.ap4k.config.Port;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.ContainerBuilder;

import java.util.Objects;

/**
 * A visitor that adds a port to all containers.
 */
public class AddPort extends TypedVisitor<ContainerBuilder> {

    private final Port port;

    public AddPort(Port port) {
        this.port = port;
    }

    @Override
    public void visit(ContainerBuilder container) {
      container.addNewPort()
        .withName(port.getName())
        .withHostPort(port.getHostPort())
        .withContainerPort(port.getContainerPort())
        .withProtocol(port.getProtocol())
        .endPort();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddPort addPortToConfig = (AddPort) o;
        return Objects.equals(port, addPortToConfig.port);
    }

    @Override
    public int hashCode() {

        return Objects.hash(port);
    }
}
