package dev.sheldan.abstracto.templating.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmbedField {
    private String name;
    private String value;
    private Boolean inline;
}
