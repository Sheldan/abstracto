package dev.sheldan.abstracto.experience.model;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendMessageToChannelLevelActionMessageModel implements LevelActionPayload {
    private MemberDisplay memberDisplay;
    private Integer level;
    private Long experience;
    private String templateKey;
}
