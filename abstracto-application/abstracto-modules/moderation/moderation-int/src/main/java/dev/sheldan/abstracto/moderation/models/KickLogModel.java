package dev.sheldan.abstracto.moderation.models;

import dev.sheldan.abstracto.command.execution.CommandTemplateContext;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

@Getter
public class KickLogModel extends CommandTemplateContext {
    private String reason;
    private Member kickingUser;
    private Member kickedUser;

    @Builder(builderMethodName = "parentBuilder")
    public KickLogModel(CommandTemplateContext commandTemplateContext, Member kickedUser, Member kickingUser, String reason) {
        super(commandTemplateContext);
        this.kickedUser = kickedUser;
        this.kickingUser = kickingUser;
        this.reason = reason;
    }
}
