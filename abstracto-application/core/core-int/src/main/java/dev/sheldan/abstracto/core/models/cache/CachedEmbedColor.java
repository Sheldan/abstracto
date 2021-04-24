package dev.sheldan.abstracto.core.models.cache;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Builder
public class CachedEmbedColor implements Serializable {
    private Integer r;
    private Integer g;
    private Integer b;
}
