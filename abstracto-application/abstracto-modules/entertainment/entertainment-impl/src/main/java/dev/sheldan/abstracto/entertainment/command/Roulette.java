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
import dev.sheldan.abstracto.entertainment.model.RouletteResponseModel;
import dev.sheldan.abstracto.entertainment.service.EntertainmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Roulette extends AbstractConditionableCommand {

    public static final String ROULETTE_RESPONSE_TEMPLATE_KEY = "roulette_response";
    @Autowired
    private ChannelService channelService;

    @Autowired
    private EntertainmentService entertainmentService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        boolean rouletteResult = entertainmentService.executeRoulette(commandContext.getAuthor());
        RouletteResponseModel responseModel = (RouletteResponseModel) ContextConverter.slimFromCommandContext(commandContext, RouletteResponseModel.class);
        responseModel.setResult(rouletteResult);
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(ROULETTE_RESPONSE_TEMPLATE_KEY, responseModel, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("roulette")
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
