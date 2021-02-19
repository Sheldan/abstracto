package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.exception.PostTargetNotValidException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.models.template.commands.PostTargetDisplayModel;
import dev.sheldan.abstracto.core.models.template.commands.PostTargetModelEntry;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
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

    public static final String POST_TARGET_SHOW_TARGETS = "posttarget_show_targets";

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        if(commandContext.getParameters().getParameters().isEmpty()) {
            log.trace("Displaying existing post targets for guild {}.", commandContext.getGuild().getId());
            PostTargetDisplayModel posttargetDisplayModel = (PostTargetDisplayModel) ContextConverter.fromCommandContext(commandContext, PostTargetDisplayModel.class);
            AServer server = serverManagementService.loadServer(commandContext.getGuild());
            List<PostTarget> postTargets = postTargetService.getPostTargets(server);
            posttargetDisplayModel.setPostTargets(new ArrayList<>());
            List<PostTargetModelEntry> postTargetEntries = posttargetDisplayModel.getPostTargets();
            postTargets.forEach(target -> {
                Optional<TextChannel> channelFromAChannel = channelService.getChannelFromAChannel(target.getChannelReference());
                PostTargetModelEntry targetEntry = PostTargetModelEntry.builder().channel(channelFromAChannel.orElse(null)).postTarget(target).build();
                postTargetEntries.add(targetEntry);
            });
            List<String> postTargetConfigs = postTargetService.getPostTargetsOfEnabledFeatures(server);
            postTargetConfigs.forEach(postTargetName -> {
                if(postTargetEntries.stream().noneMatch(postTargetModelEntry -> postTargetModelEntry.getPostTarget().getName().equalsIgnoreCase(postTargetName))) {
                    PostTarget fakeEntry = PostTarget.builder().name(postTargetName).build();
                    PostTargetModelEntry postTargetEntry = PostTargetModelEntry.builder().postTarget(fakeEntry).build();
                    postTargetEntries.add(postTargetEntry);
                }
            });
            return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(POST_TARGET_SHOW_TARGETS, posttargetDisplayModel, commandContext.getChannel()))
                    .thenApply(aVoid -> CommandResult.fromSuccess());
        }
        String targetName = (String) commandContext.getParameters().getParameters().get(0);
        if(!postTargetService.validPostTarget(targetName)) {
            throw new PostTargetNotValidException(targetName, postTargetService.getAvailablePostTargets());
        }
        GuildChannel channel = (GuildChannel) commandContext.getParameters().getParameters().get(1);
        Guild guild = channel.getGuild();
        postTargetManagement.createOrUpdate(targetName, guild.getIdLong(), channel.getIdLong());
        return CompletableFuture.completedFuture(CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter postTargetName = Parameter.builder().name("name").type(String.class).optional(true).templated(true).build();
        Parameter channel = Parameter.builder().name("channel").type(TextChannel.class).optional(true).templated(true).build();
        List<Parameter> parameters = Arrays.asList(postTargetName, channel);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("posttarget")
                .module(ChannelsModuleInterface.CHANNELS)
                .parameters(parameters)
                .async(true)
                .supportsEmbedException(true)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }
}
