package dev.sheldan.abstracto.utility.commands.suggest;

import dev.sheldan.abstracto.core.command.UtilityModuleInterface;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.template.commands.SuggestionLog;
import dev.sheldan.abstracto.utility.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Suggest extends AbstractConditionableCommand {

    @Autowired
    private SuggestionService suggestionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        suggestionService.validateSetup(commandContext.getGuild().getIdLong());
        List<Object> parameters = commandContext.getParameters().getParameters();
        String text = (String) parameters.get(0);
        SuggestionLog suggestLogModel = (SuggestionLog) ContextConverter.fromCommandContext(commandContext, SuggestionLog.class);
        suggestLogModel.setSuggester(commandContext.getAuthor());
        suggestionService.createSuggestion(commandContext.getAuthor(), text, suggestLogModel);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("text").type(String.class).templated(true).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("suggest")
                .module(UtilityModuleInterface.UTILITY)
                .templated(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.SUGGEST;
    }
}
