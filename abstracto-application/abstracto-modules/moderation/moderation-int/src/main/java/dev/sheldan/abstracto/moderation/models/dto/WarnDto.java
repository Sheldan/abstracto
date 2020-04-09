package dev.sheldan.abstracto.moderation.models.dto;

import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class WarnDto {
    private Long id;
    private UserInServerDto warnedUser;
    private UserInServerDto warningUser;
    private String reason;
    private OffsetDateTime warnDate;
    private Boolean decayed;
    private OffsetDateTime decayDate;
}
