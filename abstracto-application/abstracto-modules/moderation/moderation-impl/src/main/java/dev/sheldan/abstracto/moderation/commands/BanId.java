package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.core.converter.UserInServerModelConverter;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.Moderation;
import dev.sheldan.abstracto.moderation.config.ModerationFeatures;
import dev.sheldan.abstracto.moderation.models.template.commands.BanIdLogModel;
import dev.sheldan.abstracto.moderation.service.BanService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BanId extends AbstractConditionableCommand {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private BanService banService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private UserInServerModelConverter userConverter;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Long userId = (Long) parameters.get(0);
        String defaultReason = templateService.renderTemplateWithMap("ban_default_reason", null);
        String reason = parameters.size() == 2 ? (String) parameters.get(1) : defaultReason;
        BanIdLogModel banLogModel = (BanIdLogModel) ContextConverter.fromCommandContext(commandContext, BanIdLogModel.class);
        banLogModel.setBannedUserId(userId);
        banLogModel.setBanningUser(userConverter.fromUser(commandContext.getUserInitiatedContext().getAUserInAServer()));
        banLogModel.setReason(reason);
        banService.banMember(userId, commandContext.getGuild().getIdLong(), reason);
        banService.sendBanIdLog(banLogModel);


        return CommandResult.fromSuccess();
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

    @Override
    public String getFeature() {
        return ModerationFeatures.MODERATION;
    }
}
