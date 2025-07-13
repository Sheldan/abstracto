package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import com.google.gson.annotations.SerializedName;
import net.dv8tion.jda.api.components.separator.Separator;

public enum Spacing {
    @SerializedName("small")
    SMALL,
    @SerializedName("large")
    LARGE;

    public static Separator.Spacing toSpacing(Spacing spacing) {
        return switch (spacing) {
            case LARGE -> Separator.Spacing.LARGE;
            case SMALL -> Separator.Spacing.SMALL;
            default -> Separator.Spacing.UNKNOWN;
        };
    }
}
