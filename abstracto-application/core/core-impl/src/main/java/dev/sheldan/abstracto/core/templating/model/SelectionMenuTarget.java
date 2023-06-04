package dev.sheldan.abstracto.core.templating.model;

import com.google.gson.annotations.SerializedName;

public enum SelectionMenuTarget {
    @SerializedName("USER")
    USER,
    @SerializedName("ROLE")
    ROLE,
    @SerializedName("CHANNEL")
    CHANNEL
}
