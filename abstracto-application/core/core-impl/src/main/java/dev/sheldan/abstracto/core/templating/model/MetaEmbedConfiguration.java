package dev.sheldan.abstracto.core.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MetaEmbedConfiguration {
    private Long additionalMessageLengthLimit;
    private Long messageLimit;

    private boolean preventEmptyEmbed;
    private boolean allowsRoleMention;
    private boolean allowsEveryoneMention;
    @Builder.Default
    private boolean allowsUserMention = true;
}
