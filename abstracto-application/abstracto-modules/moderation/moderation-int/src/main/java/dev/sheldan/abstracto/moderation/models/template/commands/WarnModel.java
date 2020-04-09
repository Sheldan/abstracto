package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.template.UserInServerModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class WarnModel {
    private Long id;
    private UserInServerModel warnedUser;
    private UserInServerModel warningUser;
    private String reason;
    private OffsetDateTime warnDate;
    private Boolean decayed;
    private OffsetDateTime decayDate;
}
