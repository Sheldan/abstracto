package dev.sheldan.abstracto.core.interaction.modal.config;

import com.google.gson.annotations.SerializedName;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public enum TextInputComponentStyle {
    @SerializedName("short")
    SHORT,
    @SerializedName("paragraph")
    PARAGRAPH;

    public static TextInputStyle getStyle(TextInputComponentStyle config) {
        switch (config) {
            case PARAGRAPH:
                return TextInputStyle.PARAGRAPH;
            case SHORT:
            default:
                return TextInputStyle.SHORT;
        }
    }
}
