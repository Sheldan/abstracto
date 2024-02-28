package dev.sheldan.abstracto.experience.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddMemberToChannelLevelActionPayload implements LevelActionPayload {
    private Long channelId;
}
