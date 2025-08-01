package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import com.google.gson.annotations.SerializedName;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;

public enum ButtonStyleConfig {
    @SerializedName("primary")
    PRIMARY,
    @SerializedName("secondary")
    SECONDARY,
    @SerializedName("success")
    SUCCESS,
    @SerializedName("danger")
    DANGER,
    @SerializedName("link")
    LINK;

    public static ButtonStyle getStyle(ButtonStyleConfig config) {
        switch (config) {
            case PRIMARY: return ButtonStyle.PRIMARY;
            case SECONDARY: return ButtonStyle.SECONDARY;
            case SUCCESS: return ButtonStyle.SUCCESS;
            case DANGER: return ButtonStyle.DANGER;
            case LINK: default: return ButtonStyle.LINK;
        }
    }
}
