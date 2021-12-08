package dev.sheldan.abstracto.core.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MetaEmbedConfiguration {
    private Integer descriptionMessageLengthLimit;
    private boolean preventEmptyEmbed;
}
