package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class AddToChannelGroup extends AbstractConditionableCommand {


    public static final String ADD_TO_CHANNEL_GROUP_COMMAND = "addToChannelGroup";
    public static final String CHANNEL_PARAMETER = "channel";
    public static final String NAME_PARAMETER = "name";
    private static final String ADD_TO_CHANNEL_GROUP_RESPONSE_TEMPLATE = "addToChannelGroup_response";

    @Autowired
    private ChannelGroupService channelGroupService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String channelGroupName = slashCommandParameterService.getCommandOption(NAME_PARAMETER, event, String.class);
        GuildChannel channel = slashCommandParameterService.getCommandOption(CHANNEL_PARAMETER, event, TextChannel.class, GuildChannel.class);
        if(!channel.getGuild().equals(event.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        channelGroupService.addChannelToChannelGroup(channelGroupName, channel);
        return interactionService.replyEmbed(ADD_TO_CHANNEL_GROUP_RESPONSE_TEMPLATE, event)
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
        Parameter channelToAdd = Parameter
                .builder()
                .name(CHANNEL_PARAMETER)
                .type(TextChannel.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(channelGroupName, channelToAdd);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .hasExample(true)
                .build();
        List<String> aliases = Arrays.asList("addTChGrp", "chGrpCh+");

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .rootCommandName(CoreSlashCommandNames.CHANNELS)
                .commandName(ADD_TO_CHANNEL_GROUP_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(ADD_TO_CHANNEL_GROUP_COMMAND)
                .module(ChannelsModuleDefinition.CHANNELS)
                .aliases(aliases)
                .slashCommandOnly(true)
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
