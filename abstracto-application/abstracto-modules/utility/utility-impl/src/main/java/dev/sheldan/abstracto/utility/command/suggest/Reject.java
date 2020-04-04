package dev.sheldan.abstracto.utility.command.suggest;

import dev.sheldan.abstracto.core.command.AbstractFeatureFlaggedCommand;
import dev.sheldan.abstracto.core.command.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.utility.Utility;
import dev.sheldan.abstracto.utility.config.UtilityFeatures;
import dev.sheldan.abstracto.utility.models.template.SuggestionLog;
import dev.sheldan.abstracto.utility.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Reject extends AbstractFeatureFlaggedCommand {

    @Autowired
    private SuggestionService suggestionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        suggestionService.validateSetup(commandContext.getGuild().getIdLong());
        List<Object> parameters = commandContext.getParameters().getParameters();
        Long suggestionId = (Long) parameters.get(0);
        String text = parameters.size() == 2 ? (String) parameters.get(1) : "";
        SuggestionLog suggestionModel = (SuggestionLog) ContextConverter.fromCommandContext(commandContext, SuggestionLog.class);
        suggestionService.rejectSuggestion(suggestionId, text, suggestionModel);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("suggestionId").type(Long.class).optional(false).build());
        parameters.add(Parameter.builder().name("text").type(String.class).optional(true).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("reject")
                .module(Utility.UTILITY)
                .templated(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public String getFeature() {
        return UtilityFeatures.SUGGEST;
    }
}
