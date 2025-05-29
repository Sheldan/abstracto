package dev.sheldan.abstracto.core.templating.model.messagecomponents;


import com.google.gson.annotations.SerializedName;

public interface ComponentConfig {
    ComponentTypeConfig getType();

    enum ComponentTypeConfig {
        @SerializedName("actionRow")
        ACTION_ROW,
        @SerializedName("section")
        SECTION,
        @SerializedName("textDisplay")
        TEXT_DISPLAY,
        @SerializedName("mediaGallery")
        MEDIA_GALLERY,
        @SerializedName("separator")
        SEPARATOR,
        @SerializedName("fileDisplay")
        FILE_DISPLAY,
        @SerializedName("container")
        CONTAINER
    }
}


