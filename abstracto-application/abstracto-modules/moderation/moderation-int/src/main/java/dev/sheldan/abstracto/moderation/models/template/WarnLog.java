package dev.sheldan.abstracto.moderation.models.template;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.moderation.models.Warning;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;


@Getter @SuperBuilder @Setter
public class WarnLog extends UserInitiatedServerContext {

    private String reason;
    private Member warnedUser;
    private Member warningUser;
    private Message message;
    private Warning warning;
}
