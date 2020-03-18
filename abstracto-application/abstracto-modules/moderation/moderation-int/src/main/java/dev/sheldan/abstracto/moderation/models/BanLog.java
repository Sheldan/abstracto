package dev.sheldan.abstracto.moderation.models;

import dev.sheldan.abstracto.command.execution.CommandTemplateContext;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

@Getter
public class BanLog extends CommandTemplateContext {

    private String reason;
    private Member banningUser;
    private Member bannedUser;

    @Builder(builderMethodName = "parentBuilder")
    public BanLog(CommandTemplateContext commandTemplateContext, Member bannedUser, Member banningUser, String reason) {
        super(commandTemplateContext);
        this.bannedUser = bannedUser;
        this.banningUser = banningUser;
        this.reason = reason;
    }
}
