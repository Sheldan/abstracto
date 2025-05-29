package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Builder
@Getter
public class SectionButton extends ButtonConfig implements SectionAccessoryConfig {
    @Override
    public SectionAccessoryType getType() {
        return SectionAccessoryType.BUTTON;
    }
}

