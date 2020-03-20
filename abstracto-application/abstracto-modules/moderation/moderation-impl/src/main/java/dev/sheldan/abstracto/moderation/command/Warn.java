package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.command.execution.*;
import dev.sheldan.abstracto.moderation.Moderation;
import dev.sheldan.abstracto.moderation.models.template.WarnLog;
import dev.sheldan.abstracto.moderation.service.WarnService;
import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.HelpInfo;
import dev.sheldan.abstracto.core.management.UserManagementService;
import dev.sheldan.abstracto.templating.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class Warn implements Command {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private WarnService warnService;

    @Autowired
    private TemplateService templateService;

    @Override
    public Result execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member member = (Member) parameters.get(0);
        String defaultReason = templateService.renderTemplate("warn_default_reason", null);
        String reason = parameters.size() == 2 ? (String) parameters.get(1) : defaultReason;
        WarnLog warnLogModel = (WarnLog) ContextConverter.fromCommandContext(commandContext, WarnLog.class);
        warnLogModel.setWarnedUser(member);
        warnLogModel.setReason(reason);
        warnLogModel.setWarningUser(commandContext.getAuthor());
        warnLogModel.setMessageLink(commandContext.getMessage().getJumpUrl());
        warnService.warnUser(member, commandContext.getAuthor(), reason, warnLogModel);
        return Result.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("user").type(Member.class).optional(false).build());
        parameters.add(Parameter.builder().name("reason").type(String.class).optional(true).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("warn")
                .module(Moderation.MODERATION)
                .templated(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }
}
