package dev.sheldan.abstracto.suggestion.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberNameDisplay;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PollDecisionNotificationModel {
    private List<String> chosenValues;
    private Long pollId;
    private Long serverId;
    private MemberNameDisplay memberNameDisplay;
}
