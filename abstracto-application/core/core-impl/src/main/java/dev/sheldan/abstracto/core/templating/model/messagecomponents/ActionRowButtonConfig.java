package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ActionRowButtonConfig extends ButtonConfig implements ActionRowItemConfig{

    @Override
    public ActionRowItemType getType() {
        return ActionRowItemType.BUTTON;
    }
}
