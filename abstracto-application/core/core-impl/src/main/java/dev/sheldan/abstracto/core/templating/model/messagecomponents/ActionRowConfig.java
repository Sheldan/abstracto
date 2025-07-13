package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class ActionRowConfig {
    protected List<ActionRowItemConfig> actionRowItems;

}
