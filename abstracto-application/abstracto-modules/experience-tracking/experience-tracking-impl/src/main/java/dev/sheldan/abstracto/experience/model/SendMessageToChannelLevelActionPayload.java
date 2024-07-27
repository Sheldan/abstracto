package dev.sheldan.abstracto.experience.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendMessageToChannelLevelActionPayload implements LevelActionPayload {
    private Long channelId;
    private String templateKey;
}
