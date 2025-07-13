package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class ContainerConfig implements ComponentConfig {
    private List<ComponentConfig> components;
    @Override
    public ComponentTypeConfig getType() {
        return ComponentTypeConfig.CONTAINER;
    }
}
