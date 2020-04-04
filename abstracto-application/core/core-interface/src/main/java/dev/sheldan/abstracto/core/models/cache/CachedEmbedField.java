package dev.sheldan.abstracto.core.models.cache;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CachedEmbedField {
    private String name;
    private String value;
    private Boolean inline;
}
