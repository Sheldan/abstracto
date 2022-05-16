package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.command.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandParameterService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class DisableChannelGroup extends AbstractConditionableCommand {

    private static final String CHANNEL_GROUP_NAME_PARAMETER = "channelGroupName";
    private static final String DISABLE_CHANNEL_GROUP_COMMAND = "disableChannelGroup";
    private static final String DISABLE_CHANNEL_GROUP_RESPONSE = "disableChannelGroup_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private ChannelGroupService channelGroupService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String channelGroupName = (String) commandContext.getParameters().getParameters().get(0);
        channelGroupService.disableChannelGroup(channelGroupName, commandContext.getGuild().getIdLong());
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String channelGroupName = slashCommandParameterService.getCommandOption(CHANNEL_GROUP_NAME_PARAMETER, event, String.class);
        channelGroupService.disableChannelGroup(channelGroupName, event.getGuild().getIdLong());
        return interactionService.replyEmbed(DISABLE_CHANNEL_GROUP_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelGroupName = Parameter
                .builder()
                .name(CHANNEL_GROUP_NAME_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(channelGroupName);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(CoreSlashCommandNames.CHANNELS)
                .commandName(DISABLE_CHANNEL_GROUP_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(DISABLE_CHANNEL_GROUP_COMMAND)
                .module(ChannelsModuleDefinition.CHANNELS)
                .parameters(parameters)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
