package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.exception.SlashCommandParameterMissingException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.exception.PostTargetNotValidException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.models.template.commands.PostTargetDisplayModel;
import dev.sheldan.abstracto.core.models.template.commands.PostTargetModelEntry;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PostTargetCommand extends AbstractConditionableCommand {

    private static final String POST_TARGET_SHOW_TARGETS = "posttarget_show_targets";
    private static final String POSTTARGET_COMMAND = "posttarget";
    private static final String NAME_PARAMETER = "name";
    private static final String CHANNEL_PARAMETER = "channel";
    private static final String POSTTARGET_RESPONSE_TEMPLATE = "posttarget_response";

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Guild guild = commandContext.getGuild();
        if(commandContext.getParameters().getParameters().isEmpty()) {
            log.debug("Displaying existing post targets for guild {}.", guild.getId());
            MessageToSend messageToSend = getMessageToSendForPosttargetDisplay(guild);
            return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                    .thenApply(aVoid -> CommandResult.fromSuccess());
        }
        String targetName = (String) commandContext.getParameters().getParameters().get(0);
        GuildChannel channel = (GuildChannel) commandContext.getParameters().getParameters().get(1);
        validateAndCreatePosttarget(guild, targetName, channel);
        return CompletableFuture.completedFuture(CommandResult.fromSuccess());
    }

    private void validateAndCreatePosttarget(Guild guild, String targetName, GuildChannel channel) {
        if(!postTargetService.validPostTarget(targetName)) {
            throw new PostTargetNotValidException(targetName, postTargetService.getAvailablePostTargets());
        }
        if(!channel.getGuild().equals(guild)) {
            throw new EntityGuildMismatchException();
        }
        postTargetManagement.createOrUpdate(targetName, guild.getIdLong(), channel.getIdLong());
    }

    private MessageToSend getMessageToSendForPosttargetDisplay(Guild guild) {
        AServer server = serverManagementService.loadServer(guild);
        List<PostTarget> postTargets = postTargetService.getPostTargets(server);
        ArrayList<PostTargetModelEntry> postTargetEntries = new ArrayList<>();
        postTargets.forEach(target -> {
            Optional<GuildMessageChannel> channelFromAChannel = channelService.getGuildMessageChannelFromAChannelOptional(target.getChannelReference());
            PostTargetModelEntry targetEntry = PostTargetModelEntry
                    .builder()
                    .channel(channelFromAChannel.orElse(null))
                    .disabled(target.getDisabled())
                    .postTarget(target).build();
            postTargetEntries.add(targetEntry);
        });
        PostTargetDisplayModel posttargetDisplayModel = PostTargetDisplayModel
                .builder()
                .postTargets(postTargetEntries)
                .build();

        List<String> postTargetConfigs = postTargetService.getPostTargetsOfEnabledFeatures(server);
        postTargetConfigs.forEach(postTargetName -> {
            if(postTargetEntries.stream().noneMatch(postTargetModelEntry -> postTargetModelEntry.getPostTarget().getName().equalsIgnoreCase(postTargetName))) {
                PostTarget fakeEntry = PostTarget
                        .builder()
                        .name(postTargetName)
                        .build();
                PostTargetModelEntry postTargetEntry = PostTargetModelEntry
                        .builder()
                        .postTarget(fakeEntry)
                        .disabled(false)
                        .build();
                postTargetEntries.add(postTargetEntry);
            }
        });
        return templateService.renderEmbedTemplate(POST_TARGET_SHOW_TARGETS, posttargetDisplayModel, guild.getIdLong());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        if(!slashCommandParameterService.hasCommandOption(CHANNEL_PARAMETER, event) && !slashCommandParameterService.hasCommandOption(NAME_PARAMETER, event)) {
            MessageToSend messageToSend = getMessageToSendForPosttargetDisplay(event.getGuild());
            return interactionService.replyMessageToSend(messageToSend, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
        } else {
            if(!slashCommandParameterService.hasCommandOption(NAME_PARAMETER, event)) {
                throw new SlashCommandParameterMissingException(NAME_PARAMETER);
            }
            if(!slashCommandParameterService.hasCommandOption(CHANNEL_PARAMETER, event)) {
                throw new SlashCommandParameterMissingException(CHANNEL_PARAMETER);
            }
            String postTargetName = slashCommandParameterService.getCommandOption(NAME_PARAMETER, event, String.class);
            GuildChannel channel = slashCommandParameterService.getCommandOption(CHANNEL_PARAMETER, event, TextChannel.class, GuildChannel.class);
            validateAndCreatePosttarget(event.getGuild(), postTargetName, channel);
            return interactionService.replyEmbed(POSTTARGET_RESPONSE_TEMPLATE, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter postTargetName = Parameter
                .builder()
                .name(NAME_PARAMETER)
                .type(String.class)
                .optional(true)
                .templated(true)
                .build();
        Parameter channel = Parameter
                .builder()
                .name(CHANNEL_PARAMETER)
                .type(TextChannel.class)
                .optional(true)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(postTargetName, channel);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .hasExample(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(CoreSlashCommandNames.POST_TARGET)
                .commandName(POSTTARGET_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(POSTTARGET_COMMAND)
                .module(ChannelsModuleDefinition.CHANNELS)
                .parameters(parameters)
                .async(true)
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
