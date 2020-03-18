package dev.sheldan.abstracto.moderation.models;

import dev.sheldan.abstracto.command.execution.CommandTemplateContext;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;


@Getter
public class WarnLog extends CommandTemplateContext {

    private Warning warning;
    private Member warnedUser;
    private Member warningUser;

    @Builder(builderMethodName = "parentBuilder")
    public WarnLog(CommandTemplateContext commandTemplateContext, Warning warning, Member warnedUser, Member warningUser) {
        super(commandTemplateContext);
        this.warning = warning;
        this.warnedUser = warnedUser;
        this.warningUser = warningUser;
    }
}
