package dev.sheldan.abstracto.suggestion.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberNameDisplay;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PollAddOptionNotificationModel {
    private String label;
    private String description;
    private String value;
    private Long pollId;
    private Long serverId;
    private MemberNameDisplay memberNameDisplay;
}
