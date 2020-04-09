package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.template.UserInServerModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;

@Getter
@SuperBuilder
@Setter
public class BanIdLogModel extends UserInitiatedServerContext {
    private String reason;
    private UserInServerModel banningUser;
    private Long bannedUserId;

}
