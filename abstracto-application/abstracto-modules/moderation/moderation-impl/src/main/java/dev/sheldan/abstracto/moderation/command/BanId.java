package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.template.command.BanIdLog;
import dev.sheldan.abstracto.moderation.service.BanService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class BanId extends AbstractConditionableCommand {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private BanService banService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Long userId = (Long) parameters.get(0);
        String defaultReason = templateService.renderSimpleTemplate(Ban.BAN_DEFAULT_REASON_TEMPLATE, commandContext.getGuild().getIdLong());
        String reason = parameters.size() == 2 ? (String) parameters.get(1) : defaultReason;
        BanIdLog banLogModel = (BanIdLog) ContextConverter.fromCommandContext(commandContext, BanIdLog.class);
        banLogModel.setBannedUserId(userId);
        banLogModel.setBanningUser(commandContext.getAuthor());
        banLogModel.setReason(reason);
        return banService.banUserViaId(commandContext.getGuild().getIdLong(), userId, reason, banLogModel)
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("user").type(Long.class).templated(true).build());
        parameters.add(Parameter.builder().name("reason").type(String.class).optional(true).remainder(true).templated(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("banId")
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }
}
