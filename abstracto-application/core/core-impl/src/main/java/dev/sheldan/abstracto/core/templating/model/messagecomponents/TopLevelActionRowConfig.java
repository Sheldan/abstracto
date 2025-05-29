package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class TopLevelActionRowConfig extends ActionRowConfig implements ComponentConfig {
    @Override
    public ComponentTypeConfig getType() {
        return ComponentTypeConfig.ACTION_ROW;
    }
}
