package dev.sheldan.abstracto.templating.embed;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmbedField {
    private String name;
    private String value;
    private Boolean inline;
}
