package dev.sheldan.abstracto.core.models.cache;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class CachedEmbedColor {
    private Integer r;
    private Integer g;
    private Integer b;
}
