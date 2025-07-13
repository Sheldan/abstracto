package dev.sheldan.abstracto.core.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageConfig {
    private Boolean allowsRoleMention;
    private Boolean allowsEveryoneMention;
    @Builder.Default
    private Boolean allowsUserMention = true;
    @Builder.Default
    private Boolean mentionsReferencedMessage = true;
}
