package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import com.google.gson.annotations.SerializedName;

public interface ActionRowItemConfig {
    ActionRowItemType getType();

    enum ActionRowItemType {
        @SerializedName("button")
        BUTTON,
        @SerializedName("stringSelectMenu")
        STRING_SELECT_MENU,
        @SerializedName("entitySelectMenu")
        ENTITY_SELECT_MENU
    }

}

