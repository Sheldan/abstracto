package dev.sheldan.abstracto.suggestion.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberNameDisplay;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PollClosingMessageModel {
    private Long pollId;
    private Long serverId;
    private MemberNameDisplay cause;
    private String text;
    private Long pollMessageId;
}
