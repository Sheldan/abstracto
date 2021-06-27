package dev.sheldan.abstracto.core.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MetaEmbedConfiguration {
    private Integer additionalMessageLengthLimit;
    private Integer additionalMessageSplitLength;
    private Integer descriptionMessageLengthLimit;
    private Integer messageLimit;


    private boolean preventEmptyEmbed;
    private boolean allowsRoleMention;
    private boolean allowsEveryoneMention;
    @Builder.Default
    private boolean allowsUserMention = true;
    @Builder.Default
    private boolean mentionsReferencedMessage = true;
}
