package dev.sheldan.abstracto.suggestion.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.suggestion.config.SuggestionFeatureDefinition;
import dev.sheldan.abstracto.suggestion.config.SuggestionSlashCommandNames;
import dev.sheldan.abstracto.suggestion.service.PollService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Poll extends AbstractConditionableCommand {

    private static final String POLL_COMMAND = "poll";
    private static final String ALLOW_MULTIPLE_PARAMETER = "allowMultiple";
    private static final String SHOW_DECISIONS_PARAMETER = "showDecisions";
    private static final String ALLOW_ADDITIONS_PARAMETER = "allowAdditions";
    private static final String POLL_DURATION_PARAMETER = "pollDuration";
    private static final String POLL_DESCRIPTION_PARAMETER = "description";
    private static final String POLL_OPTIONS_PARAMETER = "options";
    private static final Integer OPTIONS_COUNT = 15;
    private static final String POLL_RESPONSE_TEMPLATE_KEY = "poll_server_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private PollService pollService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        List<String> options = new ArrayList<>();
        for (int i = 0; i < OPTIONS_COUNT; i++) {
            if(slashCommandParameterService.hasCommandOption(POLL_OPTIONS_PARAMETER + "_" + i, event)) {
                String choice = slashCommandParameterService.getCommandOption(POLL_OPTIONS_PARAMETER + "_" + i, event, String.class);
                options.add(choice);
            }
        }
        Boolean allowMultiple = false;
        if(slashCommandParameterService.hasCommandOption(ALLOW_MULTIPLE_PARAMETER, event)) {
            allowMultiple = slashCommandParameterService.getCommandOption(ALLOW_MULTIPLE_PARAMETER, event, Boolean.class);
        }

        Boolean showDecisions = false;
        if(slashCommandParameterService.hasCommandOption(SHOW_DECISIONS_PARAMETER, event)) {
            showDecisions = slashCommandParameterService.getCommandOption(SHOW_DECISIONS_PARAMETER, event, Boolean.class);
        }

        Boolean allowAdditions = false;
        if(slashCommandParameterService.hasCommandOption(ALLOW_ADDITIONS_PARAMETER, event)) {
            allowAdditions = slashCommandParameterService.getCommandOption(ALLOW_ADDITIONS_PARAMETER, event, Boolean.class);
        }
        Duration pollDuration = null;
        if(slashCommandParameterService.hasCommandOption(POLL_DURATION_PARAMETER, event)) {
            String durationString = slashCommandParameterService.getCommandOption(POLL_DURATION_PARAMETER, event, Duration.class, String.class);
            pollDuration = ParseUtils.parseDuration(durationString);
        }
        Boolean actualMultiple = allowMultiple;
        Boolean actualDecisions = showDecisions;
        Boolean actualAdditions = allowAdditions;
        Duration actualDuration = pollDuration;
        String description = slashCommandParameterService.getCommandOption(POLL_DESCRIPTION_PARAMETER, event, String.class);
        return event.deferReply()
                .submit()
                .thenCompose(interactionHook -> pollService.createServerPoll(event.getMember(), options, description, actualMultiple, actualAdditions, actualDecisions, actualDuration)
                        .thenAccept(unused -> interactionService.sendMessageToInteraction(POLL_RESPONSE_TEMPLATE_KEY, new Object(), interactionHook)))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        Parameter allowMultipleParameter = Parameter
                .builder()
                .name(ALLOW_MULTIPLE_PARAMETER)
                .type(Boolean.class)
                .templated(true)
                .optional(true)
                .build();

        Parameter showDecisions = Parameter
                .builder()
                .name(SHOW_DECISIONS_PARAMETER)
                .type(Boolean.class)
                .templated(true)
                .optional(true)
                .build();

        Parameter allowAdditions = Parameter
                .builder()
                .name(ALLOW_ADDITIONS_PARAMETER)
                .type(Boolean.class)
                .templated(true)
                .optional(true)
                .build();

        Parameter description = Parameter
                .builder()
                .name(POLL_DESCRIPTION_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();

        Parameter duration = Parameter
                .builder()
                .name(POLL_DURATION_PARAMETER)
                .type(Duration.class)
                .templated(true)
                .optional(true)
                .build();

        Parameter optionsParameter = Parameter
                .builder()
                .name(POLL_OPTIONS_PARAMETER)
                .type(String.class)
                .templated(true)
                .remainder(true)
                .listSize(OPTIONS_COUNT)
                .isListParam(true)
                .build();


        List<Parameter> parameters = Arrays.asList(description, optionsParameter, allowMultipleParameter, showDecisions, allowAdditions, duration);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(SuggestionSlashCommandNames.POLL_PUBLIC)
                .commandName("server")
                .build();

        return CommandConfiguration.builder()
                .name(POLL_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandOnly(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
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
