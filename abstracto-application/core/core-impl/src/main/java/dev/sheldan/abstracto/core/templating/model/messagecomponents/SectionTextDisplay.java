package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class SectionTextDisplay extends TextDisplayConfig implements SectionComponentConfig {
    @Override
    public SectionComponentType getType() {
        return SectionComponentType.TEXT_DISPLAY;
    }
}
