package dev.sheldan.abstracto.utility.command.suggest;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.HelpInfo;
import dev.sheldan.abstracto.command.execution.*;
import dev.sheldan.abstracto.utility.Utility;
import dev.sheldan.abstracto.utility.models.template.SuggestionLog;
import dev.sheldan.abstracto.utility.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class Suggest implements Command {

    @Autowired
    private SuggestionService suggestionService;

    @Override
    public Result execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String text = (String) parameters.get(0);
        SuggestionLog suggestLogModel = (SuggestionLog) ContextConverter.fromCommandContext(commandContext, SuggestionLog.class);
        suggestLogModel.setSuggester(commandContext.getAuthor());
        suggestionService.createSuggestion(commandContext.getAuthor(), text, suggestLogModel);
        return Result.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("text").type(String.class).optional(false).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("suggest")
                .module(Utility.UTILITY)
                .templated(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }
}
