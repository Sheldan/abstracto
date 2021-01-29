package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.moderation.config.ModerationModule;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.models.template.commands.BanLog;
import dev.sheldan.abstracto.moderation.service.BanService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class Ban extends AbstractConditionableCommand {

    public static final String BAN_DEFAULT_REASON_TEMPLATE = "ban_default_reason";
    @Autowired
    private BanService banService;

    @Autowired
    private TemplateService templateService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member member = (Member) parameters.get(0);
        String defaultReason = templateService.renderSimpleTemplate(BAN_DEFAULT_REASON_TEMPLATE);
        String reason = parameters.size() == 2 ? (String) parameters.get(1) : defaultReason;

        BanLog banLogModel = (BanLog) ContextConverter.fromCommandContext(commandContext, BanLog.class);
        banLogModel.setBannedUser(member);
        banLogModel.setBanningUser(commandContext.getAuthor());
        banLogModel.setReason(reason);
        return banService.banMember(member, reason, banLogModel)
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("user").templated(true).type(Member.class).build());
        parameters.add(Parameter.builder().name("reason").templated(true).type(String.class).optional(true).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("ban")
                .module(ModerationModule.MODERATION)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.MODERATION;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(immuneUserCondition);
        return conditions;
    }
}
