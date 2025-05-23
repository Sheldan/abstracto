package dev.sheldan.abstracto.suggestion.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
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

@Slf4j
@Component
public class Veto extends AbstractConditionableCommand {

    private static final String VETO_COMMAND = "veto";
    private static final String TEXT_PARAMETER = "text";
    private static final String SUGGESTION_ID_PARAMETER = "suggestionId";
    private static final String VETO_RESPONSE = "veto_response";

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long suggestionId = slashCommandParameterService.getCommandOption(SUGGESTION_ID_PARAMETER, event, Integer.class).longValue();
        String vetoText;
        if(slashCommandParameterService.hasCommandOption(TEXT_PARAMETER, event)) {
            vetoText = slashCommandParameterService.getCommandOption(TEXT_PARAMETER, event, String.class);
        } else {
            vetoText = "";
        }
        return suggestionService.vetoSuggestion(event.getGuild().getIdLong(), suggestionId, event.getMember(), vetoText)
                .thenCompose(unused -> interactionService.replyEmbed(VETO_RESPONSE, event))
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
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .rootCommandName(SuggestionSlashCommandNames.SUGGEST)
                .commandName(VETO_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(VETO_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandOnly(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
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
