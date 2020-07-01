package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.exception.GuildException;
import dev.sheldan.abstracto.core.exception.PostTargetNotFoundException;
import dev.sheldan.abstracto.core.exception.PostTargetNotValidException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.DefaultPostTargetManagementService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
public class PostTargetServiceBean implements PostTargetService {

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Autowired
    private BotService botService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private DefaultPostTargetManagementService defaultPostTargetManagementService;

    @Override
    public CompletableFuture<Message> sendTextInPostTarget(String text, PostTarget target)  {
        return channelService.sendTextToAChannel(text, target.getChannelReference());
    }

    @Override
    public CompletableFuture<Message>  sendEmbedInPostTarget(MessageEmbed embed, PostTarget target)  {
        TextChannel textChannelForPostTarget = getTextChannelForPostTarget(target);
        return textChannelForPostTarget.sendMessage(embed).submit();
    }

    private TextChannel getTextChannelForPostTarget(PostTarget target)  {
        Guild guild = botService.getInstance().getGuildById(target.getServerReference().getId());
        if(guild != null) {
            TextChannel textChannelById = guild.getTextChannelById(target.getChannelReference().getId());
            if(textChannelById != null) {
                return textChannelById;
            } else {
                log.error("Incorrect post target configuration: {} points to {} on server {}", target.getName(),
                        target.getChannelReference().getId(), target.getServerReference().getId());
                throw new ChannelNotFoundException(target.getChannelReference().getId(), target.getServerReference().getId());
            }
        } else {
            throw new GuildException(target.getServerReference().getId());
        }
    }

    private PostTarget getPostTarget(PostTargetEnum postTargetName, Long serverId) {
        PostTarget postTarget = postTargetManagement.getPostTarget(postTargetName.getKey(), serverId);
        if(postTarget != null) {
            return postTarget;
        } else {
            log.error("PostTarget {} in server {} was not found!", postTargetName, serverId);
            throw new PostTargetNotFoundException(postTargetName.getKey());
        }
    }

    @Override
    public CompletableFuture<Message>  sendTextInPostTarget(String text, PostTargetEnum postTargetEnum, Long serverId)  {
        PostTarget postTarget = this.getPostTarget(postTargetEnum, serverId);
        return this.sendTextInPostTarget(text, postTarget);
    }

    @Override
    public CompletableFuture<Message>  sendEmbedInPostTarget(MessageEmbed embed, PostTargetEnum postTargetName, Long serverId)  {
        PostTarget postTarget = this.getPostTarget(postTargetName, serverId);
        return this.sendEmbedInPostTarget(embed, postTarget);
    }

    @Override
    public CompletableFuture<Message> sendMessageInPostTarget(Message message, PostTargetEnum postTargetName, Long serverId) {
        PostTarget postTarget = this.getPostTarget(postTargetName, serverId);
        return sendMessageInPostTarget(message, postTarget);
    }

    @Override
    public CompletableFuture<Message> sendMessageInPostTarget(Message message, PostTarget target) {
        return channelService.sendMessageToAChannel(message, target.getChannelReference());
    }

    @Override
    public List<CompletableFuture<Message>> sendEmbedInPostTarget(MessageToSend message, PostTargetEnum postTargetName, Long serverId)  {
        PostTarget postTarget = this.getPostTarget(postTargetName, serverId);
        return this.sendEmbedInPostTarget(message, postTarget);
    }

    @Override
    public List<CompletableFuture<Message>> sendEmbedInPostTarget(MessageToSend message, PostTarget target)  {
        TextChannel textChannelForPostTarget = getTextChannelForPostTarget(target);
        return channelService.sendMessageToSendToChannel(message, textChannelForPostTarget);
    }

    @Override
    public List<CompletableFuture<Message>> editEmbedInPostTarget(Long messageId, MessageToSend message, PostTarget target)  {
        TextChannel textChannelForPostTarget = getTextChannelForPostTarget(target);
        String messageText = message.getMessage();
        if(StringUtils.isBlank(messageText)) {
            return Arrays.asList(textChannelForPostTarget.editMessageById(messageId, message.getEmbeds().get(0)).submit());
        } else {
            return Arrays.asList(textChannelForPostTarget.editMessageById(messageId, messageText).embed(message.getEmbeds().get(0)).submit());
        }
    }

    @Override
    public List<CompletableFuture<Message>> editOrCreatedInPostTarget(Long messageId, MessageToSend messageToSend, PostTarget target)  {
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        TextChannel textChannelForPostTarget = getTextChannelForPostTarget(target);
        CompletableFuture<Message> messageEditFuture = new CompletableFuture<>();
        futures.add(messageEditFuture);
        if(StringUtils.isBlank(messageToSend.getMessage().trim())) {
            textChannelForPostTarget
                    .retrieveMessageById(messageId)
                    .queue(
                            existingMessage -> existingMessage
                                    .editMessage(messageToSend.getEmbeds().get(0))
                                    .queue(messageEditFuture::complete, messageEditFuture::completeExceptionally),
                            throwable ->
                                sendEmbedInPostTarget(messageToSend, target).get(0)
                                            .thenAccept(messageEditFuture::complete).exceptionally(innerThrowable -> {
                                    log.error("Failed to send message to create a message.", innerThrowable);
                                    messageEditFuture.completeExceptionally(innerThrowable);
                                    return null;
                                })
                            );
        } else {
            textChannelForPostTarget
                    .retrieveMessageById(messageId)
                    .queue(
                            existingMessage -> existingMessage
                                    .editMessage(messageToSend.getMessage())
                                    .embed(messageToSend.getEmbeds().get(0))
                                    .queue(messageEditFuture::complete, messageEditFuture::completeExceptionally),
                            throwable ->
                                sendEmbedInPostTarget(messageToSend, target).get(0)
                                        .thenAccept(messageEditFuture::complete).exceptionally(innerThrowable -> {
                                    log.error("Failed to send message to create a message.", innerThrowable);
                                    messageEditFuture.completeExceptionally(innerThrowable);
                                    return null;
                                })
                            );
        }

        return futures;
    }

    @Override
    public List<CompletableFuture<Message>> editOrCreatedInPostTarget(Long messageId, MessageToSend messageToSend, PostTargetEnum postTargetName, Long serverId)  {
        PostTarget postTarget = this.getPostTarget(postTargetName, serverId);
        return this.editOrCreatedInPostTarget(messageId, messageToSend, postTarget);
    }

    @Override
    public void throwIfPostTargetIsNotDefined(PostTargetEnum name, Long serverId) {
        PostTarget postTarget = postTargetManagement.getPostTarget(name.getKey(), serverId);
        if(postTarget == null) {
            throw new PostTargetNotValidException(name.getKey(), defaultPostTargetManagementService.getDefaultPostTargetKeys());
        }
    }

    @Override
    public List<CompletableFuture<Message>> editEmbedInPostTarget(Long messageId, MessageToSend message, PostTargetEnum postTargetName, Long serverId)  {
        PostTarget postTarget = this.getPostTarget(postTargetName, serverId);
        return editEmbedInPostTarget(messageId, message, postTarget);
    }

    @Override
    public boolean validPostTarget(String name) {
        List<String> possiblePostTargets = defaultPostTargetManagementService.getDefaultPostTargetKeys();
        return possiblePostTargets.contains(name);
    }

    @Override
    public List<PostTarget> getPostTargets(AServer server) {
        return postTargetManagement.getPostTargetsInServer(server);
    }

    @Override
    public List<String> getAvailablePostTargets() {
        return defaultPostTargetManagementService.getDefaultPostTargetKeys();
    }

    @Override
    public List<String> getPostTargetsOfEnabledFeatures(AServer server) {
        List<String> postTargets = new ArrayList<>();
        List<FeatureConfig> allFeatureConfigs = featureConfigService.getAllFeatureConfigs();
        allFeatureConfigs.forEach(featureConfig -> {
            if(featureFlagService.isFeatureEnabled(featureConfig, server)) {
                featureConfig.getRequiredPostTargets().forEach(postTargetEnum -> postTargets.add(postTargetEnum.getKey()));
            }
        });
        return postTargets;
    }
}
