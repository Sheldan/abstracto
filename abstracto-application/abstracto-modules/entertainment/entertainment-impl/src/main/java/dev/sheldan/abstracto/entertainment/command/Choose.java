package dev.sheldan.abstracto.entertainment.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.model.command.ChooseResponseModel;
import dev.sheldan.abstracto.entertainment.service.EntertainmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Choose extends AbstractConditionableCommand {

    public static final String CHOOSE_RESPONSE_TEMPLATE_KEY = "choose_response";
    @Autowired
    private EntertainmentService entertainmentService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<String> choices = (List) commandContext.getParameters().getParameters().get(0);
        String choice = entertainmentService.takeChoice(choices, commandContext.getAuthor());
        ChooseResponseModel responseModel = (ChooseResponseModel) ContextConverter.slimFromCommandContext(commandContext, ChooseResponseModel.class);
        responseModel.setChosenValue(choice);
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(CHOOSE_RESPONSE_TEMPLATE_KEY, responseModel, commandContext.getChannel()))
            .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("text").type(String.class).templated(true).remainder(true).isListParam(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("choose")
                .async(true)
                .module(EntertainmentModuleDefinition.ENTERTAINMENT)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return EntertainmentFeatureDefinition.ENTERTAINMENT;
    }
}
