package dev.sheldan.abstracto.suggestion.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.ParameterValidator;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.suggestion.config.SuggestionFeatureDefinition;
import dev.sheldan.abstracto.suggestion.config.SuggestionSlashCommandNames;
import dev.sheldan.abstracto.suggestion.model.template.PollInfoModel;
import dev.sheldan.abstracto.suggestion.service.PollService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ShowPoll extends AbstractConditionableCommand {

    private static final String COMMAND_NAME = "show";
    private static final String SHOW_POLL_COMMAND_NAME = "showPoll";
    private static final String POLL_ID_PARAMETER = "pollId";
    private static final String SHOW_POLL_TEMPLATE_KEY = "showPoll_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private PollService pollService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long suggestionId = slashCommandParameterService.getCommandOption(POLL_ID_PARAMETER, event, Long.class, Integer.class).longValue();
        PollInfoModel pollInfoModel = pollService.getPollInfoModel(suggestionId, event.getGuild().getIdLong());
        return interactionService.replyEmbed(SHOW_POLL_TEMPLATE_KEY, pollInfoModel, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<ParameterValidator> polldIdValidator = Arrays.asList(MinIntegerValueValidator.min(1L));
        Parameter pollIdParameter = Parameter
                .builder()
                .name(POLL_ID_PARAMETER)
                .validators(polldIdValidator)
                .type(Long.class)
                .templated(true)
                .build();

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .hasExample(false)
                .build();

        List<Parameter> parameters = Arrays.asList(pollIdParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(SuggestionSlashCommandNames.POLL)
                .commandName(COMMAND_NAME)
                .build();

        return CommandConfiguration.builder()
                .name(SHOW_POLL_COMMAND_NAME)
                .slashCommandConfig(slashCommandConfig)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .slashCommandOnly(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return SuggestionFeatureDefinition.POLL;
    }
}
