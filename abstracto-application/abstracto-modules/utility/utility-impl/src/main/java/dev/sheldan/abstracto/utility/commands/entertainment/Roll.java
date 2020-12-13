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
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.utility.config.EntertainmentModuleInterface;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.template.commands.RollResponseModel;
import dev.sheldan.abstracto.utility.service.EntertainmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.utility.config.features.EntertainmentFeature.ROLL_DEFAULT_HIGH_KEY;

@Component
public class Roll extends AbstractConditionableCommand {

    public static final String ROLL_RESPONSE_TEMPLATE_KEY = "roll_response";

    @Autowired
    private ChannelService channelService;

    @Autowired
    private EntertainmentService entertainmentService;

    @Autowired
    private ConfigService configService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Integer high = configService.getLongValue(ROLL_DEFAULT_HIGH_KEY, commandContext.getGuild().getIdLong()).intValue();
        Integer low = 1;
        if(parameters.size() > 1) {
            low = (Integer) parameters.get(1);
        }
        if(!parameters.isEmpty()) {
            high = (Integer) parameters.get(0);
        }

        Integer rolled = entertainmentService.calculateRollResult(low, high);
        RollResponseModel model = (RollResponseModel) ContextConverter.slimFromCommandContext(commandContext, RollResponseModel.class);
        model.setRolled(rolled);
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInChannel(ROLL_RESPONSE_TEMPLATE_KEY, model, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("high").type(Integer.class).templated(true).optional(true).build());
        parameters.add(Parameter.builder().name("low").type(Integer.class).templated(true).optional(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("roll")
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
