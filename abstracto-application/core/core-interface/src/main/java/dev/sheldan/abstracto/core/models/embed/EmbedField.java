package dev.sheldan.abstracto.core.models.embed;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmbedField {
    private String name;
    private String value;
    private Boolean inline;
}
