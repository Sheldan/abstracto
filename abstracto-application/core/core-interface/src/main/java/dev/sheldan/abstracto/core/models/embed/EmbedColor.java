package dev.sheldan.abstracto.core.models.embed;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class EmbedColor {
    private Integer r;
    private Integer g;
    private Integer b;
}
