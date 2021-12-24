package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
@Builder
public class MuteEntry {
    private Long muteId;
    private Long serverId;
    private String reason;
    private Instant muteDate;
    private Boolean muteEnded;
    private MemberDisplay mutedUser;
    private MemberDisplay mutingUser;
    private Duration muteDuration;
}
