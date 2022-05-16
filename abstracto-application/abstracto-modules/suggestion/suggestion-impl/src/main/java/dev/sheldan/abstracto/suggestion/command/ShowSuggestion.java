package dev.sheldan.abstracto.suggestion.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.suggestion.config.SuggestionFeatureDefinition;
import dev.sheldan.abstracto.suggestion.config.SuggestionSlashCommandNames;
import dev.sheldan.abstracto.suggestion.model.template.SuggestionInfoModel;
import dev.sheldan.abstracto.suggestion.service.SuggestionService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ShowSuggestion extends AbstractConditionableCommand {

    public static final String SHOW_SUGGESTION_TEMPLATE_KEY = "suggestion_info_response";
    public static final String SHOW_SUGGESTION_COMMAND = "showSuggestion";
    public static final String SUGGESTION_ID_PARAMETER = "suggestionId";

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Long suggestionId = (Long) parameters.get(0);

        SuggestionInfoModel suggestionInfoModel = suggestionService.getSuggestionInfo(commandContext.getGuild().getIdLong(), suggestionId);
        return FutureUtils.toSingleFutureGeneric(
                channelService.sendEmbedTemplateInTextChannelList(SHOW_SUGGESTION_TEMPLATE_KEY, suggestionInfoModel, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long suggestionId = slashCommandParameterService.getCommandOption(SUGGESTION_ID_PARAMETER, event, Long.class, Integer.class).longValue();
        SuggestionInfoModel suggestionInfoModel = suggestionService.getSuggestionInfo(event.getGuild().getIdLong(), suggestionId);
        return interactionService.replyEmbed(SHOW_SUGGESTION_TEMPLATE_KEY, suggestionInfoModel, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
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
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .hasExample(false)
                .build();

        List<Parameter> parameters = Arrays.asList(suggestionIdParameter);


        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(SuggestionSlashCommandNames.SUGGEST)
                .commandName(SHOW_SUGGESTION_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(SHOW_SUGGESTION_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return SuggestionFeatureDefinition.SUGGEST;
    }
}
