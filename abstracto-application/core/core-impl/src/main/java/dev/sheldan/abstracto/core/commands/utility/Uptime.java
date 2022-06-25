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
import dev.sheldan.abstracto.core.models.SystemInfo;
import dev.sheldan.abstracto.core.models.template.commands.UptimeModel;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class Uptime extends AbstractConditionableCommand {

    public static final String UPTIME_RESPONSE_TEMPLATE_KEY = "uptime_response";
    private static final String UPTIME_COMMAND = "uptime";

    @Autowired
    private BotService botService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        UptimeModel model = getModel();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(UPTIME_RESPONSE_TEMPLATE_KEY, model,  commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        UptimeModel model = getModel();
        return interactionService.replyEmbed(UPTIME_RESPONSE_TEMPLATE_KEY, model, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    private UptimeModel getModel() {
        SystemInfo systemInfo = botService.getSystemInfo();
        return UptimeModel
                .builder()
                .uptime(systemInfo.getUptime())
                .startDate(systemInfo.getStartTime())
                .build();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(UPTIME_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(UPTIME_COMMAND)
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
