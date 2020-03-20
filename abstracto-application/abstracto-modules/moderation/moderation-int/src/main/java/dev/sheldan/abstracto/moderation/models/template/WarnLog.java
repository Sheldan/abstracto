package dev.sheldan.abstracto.moderation.models.template;

import dev.sheldan.abstracto.core.models.UserInitiatedServerContext;
import dev.sheldan.abstracto.moderation.models.Warning;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;


@Getter @SuperBuilder @Setter
public class WarnLog extends UserInitiatedServerContext {

    private String reason;
    private Member warnedUser;
    private Member warningUser;

}
