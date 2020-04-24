package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;

@Getter
@SuperBuilder
@Setter
public class KickLogModel extends UserInitiatedServerContext {
    private String reason;
    private Member kickingUser;
    private Member kickedUser;
}
