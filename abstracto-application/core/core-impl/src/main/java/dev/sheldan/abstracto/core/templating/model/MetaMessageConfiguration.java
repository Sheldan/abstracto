package dev.sheldan.abstracto.core.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MetaMessageConfiguration {
    @Builder.Default
    private boolean ephemeral = false;

    private boolean allowsRoleMention;
    private boolean allowsEveryoneMention;

    @Builder.Default
    private boolean allowsUserMention = true;
    @Builder.Default
    private boolean mentionsReferencedMessage = true;

    private Integer messageLimit;

    private Integer additionalMessageLengthLimit;
    private Integer additionalMessageSplitLength;
}
