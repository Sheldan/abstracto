package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import com.google.gson.annotations.SerializedName;

public interface SectionAccessoryConfig {
    SectionAccessoryType getType();

    enum SectionAccessoryType {
        @SerializedName("button")
        BUTTON,
        @SerializedName("thumbnail")
        THUMBNAIL
    }
}


