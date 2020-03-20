package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.HelpInfo;
import dev.sheldan.abstracto.command.execution.*;
import dev.sheldan.abstracto.core.models.UserInitiatedServerContext;
import dev.sheldan.abstracto.moderation.Moderation;
import dev.sheldan.abstracto.moderation.models.template.BanIdLog;
import dev.sheldan.abstracto.moderation.service.BanService;
import dev.sheldan.abstracto.templating.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class BanId implements Command {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private BanService banService;

    @Override
    public Result execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Long userId = (Long) parameters.get(0);
        String defaultReason = templateService.renderTemplate("ban_default_reason", null);
        String reason = parameters.size() == 2 ? (String) parameters.get(1) : defaultReason;
        BanIdLog banLogModel = (BanIdLog) ContextConverter.fromCommandContext(commandContext, BanIdLog.class);
        banLogModel.setBannedUserId(userId);
        banLogModel.setBanningUser(commandContext.getAuthor());
        banLogModel.setReason(reason);
        banService.banMember(userId, commandContext.getGuild().getIdLong(), reason, banLogModel);

        return Result.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("user").type(Long.class).optional(false).build());
        parameters.add(Parameter.builder().name("reason").type(String.class).optional(true).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("banid")
                .module(Moderation.MODERATION)
                .templated(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }
}
