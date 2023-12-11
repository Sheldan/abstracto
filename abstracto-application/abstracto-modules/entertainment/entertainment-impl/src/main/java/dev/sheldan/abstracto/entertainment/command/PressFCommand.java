package dev.sheldan.abstracto.entertainment.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentSlashCommandNames;
import dev.sheldan.abstracto.entertainment.model.command.PressFPromptModel;
import dev.sheldan.abstracto.entertainment.service.EntertainmentService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureConfig.PRESS_F_DEFAULT_DURATION_SECONDS;

@Component
public class PressFCommand extends AbstractConditionableCommand {

    public static final String TEXT_PARAMETER = "text";
    public static final String DURATION_PARAMETER = "duration";

    private static final String RESPONSE_TEMPLATE = "pressF_response";
    public static final String PRESS_F_COMMAND_NAME = "pressF";

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private EntertainmentService entertainmentService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String text = (String) commandContext.getParameters().getParameters().get(0);
        Long defaultDurationSeconds = configService.getLongValueOrConfigDefault(PRESS_F_DEFAULT_DURATION_SECONDS, commandContext.getGuild().getIdLong());
        Duration duration = Duration.ofSeconds(defaultDurationSeconds);
        PressFPromptModel pressFModel = entertainmentService.getPressFModel(text);
        List<CompletableFuture<Message>> messages = channelService.sendEmbedTemplateInMessageChannelList(RESPONSE_TEMPLATE, pressFModel, commandContext.getChannel());
        return FutureUtils.toSingleFutureGeneric(messages)
                .thenAccept(unused -> entertainmentService.persistPressF(text, duration, commandContext.getAuthor(),
                                pressFModel.getPressFComponentId(), commandContext.getChannel(), messages.get(0).join().getIdLong()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String text = slashCommandParameterService.getCommandOption(TEXT_PARAMETER, event, String.class);
        Duration duration;
        if(slashCommandParameterService.hasCommandOption(DURATION_PARAMETER, event)) {
            String durationString = slashCommandParameterService.getCommandOption(DURATION_PARAMETER, event, String.class);
            duration = ParseUtils.parseDuration(durationString);
        } else {
            Long defaultDurationSeconds = configService.getLongValueOrConfigDefault(PRESS_F_DEFAULT_DURATION_SECONDS, event.getGuild().getIdLong());
            duration = Duration.ofSeconds(defaultDurationSeconds);
        }
        PressFPromptModel pressFModel = entertainmentService.getPressFModel(text);
        return interactionService.replyEmbed(RESPONSE_TEMPLATE, pressFModel, event)
                .thenCompose(interactionHook -> interactionHook.retrieveOriginal().submit())
        .thenAccept(message -> {
            entertainmentService.persistPressF(text, duration, event.getMember(), pressFModel.getPressFComponentId(), event.getGuildChannel(), message.getIdLong());
        })
        .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter textParameter = Parameter
                .builder()
                .name(TEXT_PARAMETER)
                .type(String.class)
                .templated(true)
                .remainder(true)
                .build();
        parameters.add(textParameter);
        Parameter durationParameter = Parameter
                .builder()
                .name(DURATION_PARAMETER)
                .type(Duration.class)
                .slashCommandOnly(true)
                .optional(true)
                .templated(true)
                .build();
        parameters.add(durationParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(EntertainmentSlashCommandNames.ENTERTAINMENT)
                .commandName("pressf")
                .build();

        return CommandConfiguration.builder()
                .name(PRESS_F_COMMAND_NAME)
                .module(EntertainmentModuleDefinition.ENTERTAINMENT)
                .templated(true)
                .causesReaction(false)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return EntertainmentFeatureDefinition.ENTERTAINMENT;
    }
}
