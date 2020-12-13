package dev.sheldan.abstracto.utility.commands.entertainment;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.config.EntertainmentModuleInterface;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.template.commands.EightBallResponseModel;
import dev.sheldan.abstracto.utility.service.EntertainmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class EightBall extends AbstractConditionableCommand {

    public static final String EIGHT_BALL_RESPONSE_TEMPLATE_KEY = "eight_ball_response";
    @Autowired
    private EntertainmentService entertainmentService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String text = (String) commandContext.getParameters().getParameters().get(0);
        String chosenKey = entertainmentService.getEightBallValue(text);
        EightBallResponseModel responseModel = (EightBallResponseModel) ContextConverter.slimFromCommandContext(commandContext, EightBallResponseModel.class);
        responseModel.setChosenKey(chosenKey);
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInChannel(EIGHT_BALL_RESPONSE_TEMPLATE_KEY, responseModel, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("text").type(String.class).templated(true).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("8Ball")
                .async(true)
                .module(EntertainmentModuleInterface.ENTERTAINMENT)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.ENTERTAINMENT;
    }
}
