package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class DeleteChannelGroup extends AbstractConditionableCommand {

    private static final String DELETE_CHANNEL_GROUP_COMMAND = "deleteChannelGroup";
    private static final String NAME_PARAMETER = "name";
    private static final String DELETE_CHANNEL_GROUP_RESPONSE = "deleteChannelGroup_response";

    @Autowired
    private ChannelGroupService channelGroupService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String groupName = (String) commandContext.getParameters().getParameters().get(0);
        channelGroupService.deleteChannelGroup(groupName, commandContext.getGuild().getIdLong());
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String channelGroupName = slashCommandParameterService.getCommandOption(NAME_PARAMETER, event, String.class);
        channelGroupService.deleteChannelGroup(channelGroupName, event.getGuild().getIdLong());
        return interactionService.replyEmbed(DELETE_CHANNEL_GROUP_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelGroupName = Parameter
                .builder()
                .name(NAME_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(channelGroupName);
        List<String> aliases = Arrays.asList("-ChGroup");
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(CoreSlashCommandNames.CHANNELS)
                .commandName(DELETE_CHANNEL_GROUP_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(DELETE_CHANNEL_GROUP_COMMAND)
                .module(ChannelsModuleDefinition.CHANNELS)
                .parameters(parameters)
                .aliases(aliases)
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
