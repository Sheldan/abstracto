package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import com.google.gson.annotations.SerializedName;

public interface SectionComponentConfig {
    SectionComponentType getType();

    enum SectionComponentType {
        @SerializedName("textDisplay")
        TEXT_DISPLAY
    }
}
