package dev.sheldan.abstracto.utility.models.template.commands;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@SuperBuilder
public class UserInfoModel extends UserInitiatedServerContext {
    private Member  memberInfo;
}
