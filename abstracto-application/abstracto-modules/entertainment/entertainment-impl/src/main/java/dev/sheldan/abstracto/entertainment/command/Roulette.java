package dev.sheldan.abstracto.entertainment.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.UserCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentSlashCommandNames;
import dev.sheldan.abstracto.entertainment.model.command.RouletteResponseModel;
import dev.sheldan.abstracto.entertainment.service.EntertainmentService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Roulette extends AbstractConditionableCommand {

    public static final String ROULETTE_RESPONSE_TEMPLATE_KEY = "roulette_response";
    public static final String ROULETTE_COMMAND = "roulette";
    @Autowired
    private ChannelService channelService;

    @Autowired
    private EntertainmentService entertainmentService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        boolean rouletteResult = entertainmentService.executeRoulette(commandContext.getAuthor());
        RouletteResponseModel responseModel = RouletteResponseModel
                .builder()
                .result(rouletteResult)
                .build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInMessageChannel(ROULETTE_RESPONSE_TEMPLATE_KEY, responseModel, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        boolean rouletteResult = entertainmentService.executeRoulette(event.getMember());
        RouletteResponseModel responseModel = RouletteResponseModel
                .builder()
                .result(rouletteResult)
                .build();
        return interactionService.replyEmbed(ROULETTE_RESPONSE_TEMPLATE_KEY, responseModel, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
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
                .commandName(ROULETTE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(ROULETTE_COMMAND)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
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
