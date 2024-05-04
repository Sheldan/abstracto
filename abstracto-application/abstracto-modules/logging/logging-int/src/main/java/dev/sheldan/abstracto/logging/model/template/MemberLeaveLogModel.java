package dev.sheldan.abstracto.logging.model.template;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MemberLeaveLogModel {
    private ServerUser leavingUser;
    private UserDisplay user;
}
