package dev.sheldan.abstracto.core.commands.utility;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.template.commands.PingModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class Ping extends AbstractConditionableCommand {

    public static final String PING_TEMPLATE = "ping_response";
    private static final String PING_COMMAND = "ping";

    @Autowired
    private ChannelService channelService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        PingModel model = buildModel(commandContext.getJda());
        return channelService.sendTextTemplateInMessageChannel(PING_TEMPLATE, model, commandContext.getChannel())
                .thenApply(message -> CommandResult.fromIgnored());
    }


    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        PingModel model = buildModel(event.getJDA());
        return interactionService.replyMessage(PING_TEMPLATE, model, event)
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    private PingModel buildModel(JDA jda) {
        long ping = jda.getGatewayPing();
        return PingModel
                .builder()
                .latency(ping)
                .build();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(PING_COMMAND)
                .build();
        return CommandConfiguration.builder()
                .name(PING_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .help(helpInfo)
                .slashCommandConfig(slashCommandConfig)
                .causesReaction(false)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }

}
