package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.template.command.WarnContext;
import dev.sheldan.abstracto.moderation.service.WarnService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.moderation.service.WarnService.WARN_EFFECT_KEY;

@Component
@Slf4j
public class Warn extends AbstractConditionableCommand {

    public static final String WARN_DEFAULT_REASON_TEMPLATE = "warn_default_reason";
    private static final String WARN_COMMAND = "warn";
    private static final String USER_PARAMETER = "user";
    private static final String REASON_PARAMETER = "reason";
    private static final String WARN_RESPONSE = "warn_response";

    @Autowired
    private WarnService warnService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member member = (Member) parameters.get(0);
        if(!member.getGuild().equals(commandContext.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        String defaultReason = templateService.renderSimpleTemplate(WARN_DEFAULT_REASON_TEMPLATE, commandContext.getGuild().getIdLong());
        String reason = parameters.size() == 2 ? (String) parameters.get(1) : defaultReason;
        WarnContext warnLogModel = WarnContext
                .builder()
                .reason(reason)
                .warnedMember(member)
                .channel(commandContext.getChannel())
                .member(commandContext.getAuthor())
                .guild(commandContext.getGuild())
                .message(commandContext.getMessage())
                .build();
        return warnService.warnUserWithLog(warnLogModel)
                .thenApply(warning -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Member member = slashCommandParameterService.getCommandOption(USER_PARAMETER, event, Member.class);
        if(!member.getGuild().equals(event.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        String reason;
        if(slashCommandParameterService.hasCommandOption(REASON_PARAMETER, event)) {
            reason = slashCommandParameterService.getCommandOption(REASON_PARAMETER, event, String.class);
        } else {
            reason = templateService.renderSimpleTemplate(WARN_DEFAULT_REASON_TEMPLATE, event.getGuild().getIdLong());
        }
        WarnContext warnLogModel = WarnContext
                .builder()
                .reason(reason)
                .warnedMember(member)
                .member(event.getMember())
                .channel(event.getGuildChannel())
                .guild(event.getGuild())
                .build();
        return warnService.warnUserWithLog(warnLogModel)
                .thenCompose(unused -> interactionService.replyEmbed(WARN_RESPONSE, event))
                .thenApply(warning -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        Parameter userParameter = Parameter
                .builder()
                .name(USER_PARAMETER)
                .type(Member.class)
                .templated(true)
                .build();
        Parameter reasonParameter = Parameter
                .builder()
                .name(REASON_PARAMETER)
                .type(String.class)
                .templated(true)
                .optional(true)
                .remainder(true)
                .build();
        HelpInfo helpInfo = HelpInfo
                .builder().
                templated(true)
                .hasExample(true)
                .build();
        List<Parameter> parameters = Arrays.asList(userParameter, reasonParameter);
        EffectConfig warnEffect = EffectConfig
                .builder()
                .position(0)
                .parameterName(USER_PARAMETER)
                .effectKey(WARN_EFFECT_KEY)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModerationSlashCommandNames.WARNINGS)
                .commandName("create")
                .build();

        List<EffectConfig> effectConfig = Arrays.asList(warnEffect);
        return CommandConfiguration.builder()
                .name(WARN_COMMAND)
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .causesReaction(true)
                .effects(effectConfig)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.WARNING;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(immuneUserCondition);
        return conditions;
    }
}
