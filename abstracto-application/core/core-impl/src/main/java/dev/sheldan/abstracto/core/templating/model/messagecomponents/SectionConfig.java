package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class SectionConfig {
    protected SectionAccessoryConfig accessory;
    protected List<SectionComponentConfig> components;

}
