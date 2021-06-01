package dev.sheldan.abstracto.moderation.model.template.listener;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class WarnDecayMemberNotificationModel {
    private Instant warnDate;
    private String warnReason;
    private Integer remainingWarningsCount;
}
