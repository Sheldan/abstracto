package dev.sheldan.abstracto.suggestion.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.suggestion.config.SuggestionFeatureDefinition;
import dev.sheldan.abstracto.suggestion.config.SuggestionSlashCommandNames;
import dev.sheldan.abstracto.suggestion.service.SuggestionService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class Reject extends AbstractConditionableCommand {

    private static final String REJECT_COMMAND = "reject";
    private static final String TEXT_PARAMETER = "text";
    private static final String SUGGESTION_ID_PARAMETER = "suggestionId";
    private static final String REJECT_RESPONSE = "reject_response";

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Long suggestionId = (Long) parameters.get(0);
        String text = parameters.size() == 2 ? (String) parameters.get(1) : "";
        log.debug("Using default reason for reject: {}.", parameters.size() != 2);
        return suggestionService.rejectSuggestion(suggestionId, commandContext.getAuthor(), text)
                .thenApply(aVoid ->  CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long suggestionId = slashCommandParameterService.getCommandOption(SUGGESTION_ID_PARAMETER, event, Integer.class).longValue();
        String rejectText;
        if(slashCommandParameterService.hasCommandOption(TEXT_PARAMETER, event)) {
            rejectText = slashCommandParameterService.getCommandOption(TEXT_PARAMETER, event, String.class);
        } else {
            rejectText = "";
        }
        return suggestionService.rejectSuggestion(suggestionId, event.getMember(), rejectText)
                .thenCompose(unused -> interactionService.replyEmbed(REJECT_RESPONSE, event))
                .thenApply(aVoid ->  CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        List<ParameterValidator> suggestionIdValidator = Arrays.asList(MinIntegerValueValidator.min(1L));
        Parameter suggestionIdParameter = Parameter
                .builder()
                .name(SUGGESTION_ID_PARAMETER)
                .validators(suggestionIdValidator)
                .type(Long.class)
                .templated(true)
                .build();
        Parameter textParameter = Parameter
                .builder()
                .name(TEXT_PARAMETER)
                .type(String.class)
                .optional(true)
                .remainder(true)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(suggestionIdParameter, textParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .hasExample(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(SuggestionSlashCommandNames.SUGGEST)
                .commandName(REJECT_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(REJECT_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return SuggestionFeatureDefinition.SUGGEST;
    }
}
