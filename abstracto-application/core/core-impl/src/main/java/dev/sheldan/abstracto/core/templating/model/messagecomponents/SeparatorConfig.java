package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class SeparatorConfig implements ComponentConfig {
    private Boolean divider;
    private Spacing spacing;
    @Override
    public ComponentTypeConfig getType() {
        return ComponentTypeConfig.SEPARATOR;
    }

}
