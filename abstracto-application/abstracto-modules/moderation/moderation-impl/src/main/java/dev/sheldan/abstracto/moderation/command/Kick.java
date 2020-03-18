package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.HelpInfo;
import dev.sheldan.abstracto.command.execution.CommandConfiguration;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Parameter;
import dev.sheldan.abstracto.command.execution.Result;
import dev.sheldan.abstracto.moderation.Moderation;
import dev.sheldan.abstracto.moderation.models.BanLog;
import dev.sheldan.abstracto.moderation.models.KickLogModel;
import dev.sheldan.abstracto.moderation.service.KickServiceBean;
import dev.sheldan.abstracto.templating.TemplateService;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Kick implements Command {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private KickServiceBean kickService;
    @Override
    public Result execute(CommandContext commandContext) {

        List<Object> parameters = commandContext.getParameters().getParameters();
        Member member = (Member) parameters.get(0);
        String defaultReason = templateService.renderTemplate("ban_default_reason", null);
        String reason = parameters.size() == 2 ? (String) parameters.get(1) : defaultReason;
        kickService.kickMember(member, reason);
        KickLogModel kickLogModel = KickLogModel
                .parentBuilder()
                .commandTemplateContext(commandContext.getCommandTemplateContext())
                .kickedUser(member)
                .kickingUser(commandContext.getAuthor())
                .reason(reason)
                .build();
        kickService.sendKickLog(kickLogModel);
        return Result.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("user").type(Member.class).optional(false).build());
        parameters.add(Parameter.builder().name("reason").type(String.class).optional(true).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().usageTemplate("kick_usage").longHelpTemplate("kick_long_help").build();
        return CommandConfiguration.builder()
                .name("kick")
                .module(Moderation.MODERATION)
                .descriptionTemplate("kick_help_description")
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }
}
