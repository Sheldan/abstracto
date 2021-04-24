package dev.sheldan.abstracto.core.models.cache;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class CachedEmbedField implements Serializable {
    private String name;
    private String value;
    private Boolean inline;
}
