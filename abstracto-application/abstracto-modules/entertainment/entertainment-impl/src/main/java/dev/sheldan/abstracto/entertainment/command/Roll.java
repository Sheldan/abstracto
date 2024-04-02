package dev.sheldan.abstracto.entertainment.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.UserCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureConfig;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentSlashCommandNames;
import dev.sheldan.abstracto.entertainment.model.command.RollResponseModel;
import dev.sheldan.abstracto.entertainment.service.EntertainmentService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Roll extends AbstractConditionableCommand {

    public static final String ROLL_RESPONSE_TEMPLATE_KEY = "roll_response";
    private static final String ROLL_COMMAND = "roll";
    private static final String LOW_PARAMETER = "low";
    private static final String HIGH_PARAMETER = "high";

    @Autowired
    private ChannelService channelService;

    @Autowired
    private EntertainmentService entertainmentService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Integer high = configService.getLongValueOrConfigDefault(EntertainmentFeatureConfig.ROLL_DEFAULT_HIGH_KEY, commandContext.getGuild().getIdLong()).intValue();
        Integer low = 1;
        if(parameters.size() > 1) {
            low = (Integer) parameters.get(1);
        }
        if(!parameters.isEmpty()) {
            high = (Integer) parameters.get(0);
        }

        Integer rolled = entertainmentService.calculateRollResult(low, high);
        RollResponseModel model = RollResponseModel
                .builder()
                .rolled(rolled)
                .build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInMessageChannel(ROLL_RESPONSE_TEMPLATE_KEY, model, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Integer low;
        if(slashCommandParameterService.hasCommandOption(LOW_PARAMETER, event)) {
            low = slashCommandParameterService.getCommandOption(LOW_PARAMETER, event, Integer.class);
        } else {
            low = 1;
        }
        Integer high;
        if(slashCommandParameterService.hasCommandOption(HIGH_PARAMETER, event)) {
            high = slashCommandParameterService.getCommandOption(HIGH_PARAMETER, event, Integer.class);
        } else {
            high = configService.getLongValueOrConfigDefault(EntertainmentFeatureConfig.ROLL_DEFAULT_HIGH_KEY, event.getGuild().getIdLong()).intValue();
        }
        Integer rolled = entertainmentService.calculateRollResult(low, high);
        RollResponseModel model = RollResponseModel
                .builder()
                .rolled(rolled)
                .build();
        return interactionService.replyEmbed(ROLL_RESPONSE_TEMPLATE_KEY, model, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter highParameter = Parameter
                .builder()
                .name(HIGH_PARAMETER)
                .type(Integer.class)
                .templated(true)
                .validators(Arrays.asList(MinIntegerValueValidator.min(2L)))
                .optional(true)
                .build();
        parameters.add(highParameter);
        Parameter lowParameter = Parameter
                .builder()
                .name(LOW_PARAMETER)
                .type(Integer.class)
                .templated(true)
                .validators(Arrays.asList(MinIntegerValueValidator.min(0L)))
                .optional(true)
                .build();
        parameters.add(lowParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .userInstallable(true)
                .userCommandConfig(UserCommandConfig.all())
                .rootCommandName(EntertainmentSlashCommandNames.UTILITY)
                .commandName(ROLL_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(ROLL_COMMAND)
                .slashCommandConfig(slashCommandConfig)
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
