package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
public class InfractionEntry {
    private String reason;
    private Long infractionId;
    private Long serverId;
    private Boolean decayed;
    private Instant creationDate;
    private Instant decayDate;
    private MemberDisplay infractionUser;
    private MemberDisplay infractionCreationUser;
    private String type;
    private Map<String, String> parameters;
}
