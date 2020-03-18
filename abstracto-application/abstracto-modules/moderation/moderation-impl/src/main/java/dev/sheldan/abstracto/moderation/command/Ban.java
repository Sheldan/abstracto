package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.HelpInfo;
import dev.sheldan.abstracto.command.execution.CommandConfiguration;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Parameter;
import dev.sheldan.abstracto.command.execution.Result;
import dev.sheldan.abstracto.moderation.Moderation;
import dev.sheldan.abstracto.moderation.models.BanLog;
import dev.sheldan.abstracto.moderation.models.WarnLog;
import dev.sheldan.abstracto.moderation.service.BanService;
import dev.sheldan.abstracto.templating.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.hibernate.sql.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class Ban implements Command {

    @Autowired
    private BanService banService;

    @Autowired
    private TemplateService templateService;

    @Override
    public Result execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member member = (Member) parameters.get(0);
        String defaultReason = templateService.renderTemplate("ban_default_reason", null);
        String reason = parameters.size() == 2 ? (String) parameters.get(1) : defaultReason;
        banService.banMember(member, reason);
        BanLog banLogModel = BanLog
                .parentBuilder()
                .commandTemplateContext(commandContext.getCommandTemplateContext())
                .bannedUser(member)
                .banningUser(commandContext.getAuthor())
                .reason(reason)
                .build();
        banService.sendBanLog(banLogModel);
        return Result.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("user").type(Member.class).optional(false).build());
        parameters.add(Parameter.builder().name("reason").type(String.class).optional(false).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().usageTemplate("ban_usage").longHelpTemplate("ban_long_help").build();
        return CommandConfiguration.builder()
                .name("ban")
                .module(Moderation.MODERATION)
                .descriptionTemplate("ban_help_description")
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }
}
