package dev.sheldan.abstracto.core.models.embed;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmbedFooter {
    private String text;
    private String icon;
}
