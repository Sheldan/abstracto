package dev.sheldan.abstracto.suggestion.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.ParameterValidator;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.suggestion.config.SuggestionFeatureDefinition;
import dev.sheldan.abstracto.suggestion.service.SuggestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class Reject extends AbstractConditionableCommand {

    @Autowired
    private SuggestionService suggestionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Long suggestionId = (Long) parameters.get(0);
        String text = parameters.size() == 2 ? (String) parameters.get(1) : "";
        log.debug("Using default reason for accept: {}.", parameters.size() != 2);
        return suggestionService.rejectSuggestion(suggestionId, commandContext.getMessage(), text)
                .thenApply(aVoid ->  CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();

        List<ParameterValidator> suggestionIdValidator = Arrays.asList(MinIntegerValueValidator.min(1L));
        parameters.add(Parameter.builder().name("suggestionId").validators(suggestionIdValidator).type(Long.class).templated(true).build());
        parameters.add(Parameter.builder().name("text").type(String.class).optional(true).remainder(true).templated(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("reject")
                .module(UtilityModuleDefinition.UTILITY)
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
        return SuggestionFeatureDefinition.SUGGEST;
    }
}
