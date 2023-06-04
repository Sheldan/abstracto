package dev.sheldan.abstracto.core.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SelectionMenuEntry {
    private String value;
    private String label;
    private Boolean isDefault;
    private String description;
}
