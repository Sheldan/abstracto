package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.moderation.config.ModerationModule;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.models.template.commands.BanIdLog;
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

    @Override
    public CommandResult execute(CommandContext commandContext) {
        checkParameters(commandContext);
        List<Object> parameters = commandContext.getParameters().getParameters();
        Long userId = (Long) parameters.get(0);
        String defaultReason = templateService.renderSimpleTemplate(Ban.BAN_DEFAULT_REASON_TEMPLATE);
        String reason = parameters.size() == 2 ? (String) parameters.get(1) : defaultReason;
        BanIdLog banLogModel = (BanIdLog) ContextConverter.fromCommandContext(commandContext, BanIdLog.class);
        banLogModel.setBannedUserId(userId);
        banLogModel.setBanningUser(commandContext.getAuthor());
        banLogModel.setReason(reason);
        banService.banMember(commandContext.getGuild().getIdLong(), userId, reason, banLogModel);

        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("user").type(Long.class).templated(true).build());
        parameters.add(Parameter.builder().name("reason").type(String.class).optional(true).remainder(true).templated(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("banId")
                .module(ModerationModule.MODERATION)
                .templated(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.MODERATION;
    }
}
