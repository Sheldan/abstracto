package dev.sheldan.abstracto.moderation.model.template.listener;

import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserUnBannedLogModel {
    private UserDisplay unBannedUser;
    private String reason;
    private UserDisplay unBanningUser;
}
