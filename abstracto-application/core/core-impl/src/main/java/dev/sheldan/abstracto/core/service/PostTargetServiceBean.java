package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.exception.ChannelNotInGuildException;
import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.exception.PostTargetNotUsableException;
import dev.sheldan.abstracto.core.exception.PostTargetNotValidException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.service.management.DefaultPostTargetManagementService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    private MessageService messageService;

    @Override
    public CompletableFuture<Message> sendTextInPostTarget(String text, PostTarget target)  {
        if(target.getDisabled()) {
            log.info("Post target {} has been disabled in server {} - not sending message.", target.getName(), target.getServerReference().getId());
            return CompletableFuture.completedFuture(null);
        } else {
            log.debug("Sending text to post target {}.", target.getName());
            return channelService.sendTextToAChannel(text, target.getChannelReference());
        }
    }

    @Override
    public CompletableFuture<Message>  sendEmbedInPostTarget(MessageEmbed embed, PostTarget target)  {
        if(target.getDisabled()) {
            log.info("Post target {} has been disabled in server {} - not sending message.", target.getName(), target.getServerReference().getId());
            return CompletableFuture.completedFuture(null);
        } else {
            GuildMessageChannel messageChannelForPostTarget = getMessageChannelForPostTarget(target);
            log.debug("Sending message embed to post target {}.", target.getName());
            return channelService.sendEmbedToChannel(embed, messageChannelForPostTarget);
        }
    }

    private GuildMessageChannel getMessageChannelForPostTarget(PostTarget target)  {
        Guild guild = botService.getInstance().getGuildById(target.getServerReference().getId());
        if(guild != null) {
            GuildChannel guildChannelById = guild.getGuildChannelById(target.getChannelReference().getId());
            if(guildChannelById instanceof GuildMessageChannel) {
                return (GuildMessageChannel) guildChannelById;
            } else {
                log.error("Incorrect post target configuration (it is not a message channel): {} points to {} on server {}", target.getName(),
                        target.getChannelReference().getId(), target.getServerReference().getId());
                throw new ChannelNotInGuildException(target.getChannelReference().getId());
            }
        } else {
            throw new GuildNotFoundException(target.getServerReference().getId());
        }
    }

    @Override
    public CompletableFuture<Message>  sendTextInPostTarget(String text, PostTargetEnum postTargetEnum, Long serverId)  {
        Optional<PostTarget> postTargetOptional = getPostTarget(postTargetEnum, serverId);
        if(!postTargetOptional.isPresent()) {
            return CompletableFuture.completedFuture(null);
        }
        return this.sendTextInPostTarget(text, postTargetOptional.get());
    }

    @Override
    public CompletableFuture<Message>  sendEmbedInPostTarget(MessageEmbed embed, PostTargetEnum postTargetName, Long serverId)  {
        Optional<PostTarget> postTargetOptional = getPostTarget(postTargetName, serverId);
        if(!postTargetOptional.isPresent()) {
            return CompletableFuture.completedFuture(null);
        }
        return this.sendEmbedInPostTarget(embed, postTargetOptional.get());
    }

    @Override
    public CompletableFuture<Message> sendMessageInPostTarget(Message message, PostTargetEnum postTargetName, Long serverId) {
        Optional<PostTarget> postTargetOptional = getPostTarget(postTargetName, serverId);
        if(!postTargetOptional.isPresent()) {
            return CompletableFuture.completedFuture(null);
        }
        return sendMessageInPostTarget(message, postTargetOptional.get());
    }

    @Override
    public CompletableFuture<Message> sendMessageInPostTarget(Message message, PostTarget target) {
        if(target.getDisabled()) {
            log.info("Post target {} has been disabled in server {} - not sending message.", target.getName(), target.getServerReference().getId());
            return CompletableFuture.completedFuture(null);
        } else {
            log.debug("Send message {} towards post target {}.", message.getId(), target.getName());
            return channelService.sendMessageToAChannel(message, target.getChannelReference());
        }
    }

    @Override
    public List<CompletableFuture<Message>> sendEmbedInPostTarget(MessageToSend message, PostTargetEnum postTargetName, Long serverId)  {
        Optional<PostTarget> postTargetOptional = getPostTarget(postTargetName, serverId);
        if(!postTargetOptional.isPresent()) {
            return Arrays.asList(CompletableFuture.completedFuture(null));
        }
        return this.sendEmbedInPostTarget(message, postTargetOptional.get());
    }

    @Override
    public List<CompletableFuture<Message>> sendEmbedInPostTarget(MessageToSend message, PostTarget target)  {
        if(target.getDisabled()) {
            log.info("Post target {} has been disabled in server {} - not sending message.", target.getName(), target.getServerReference().getId());
            return Arrays.asList(CompletableFuture.completedFuture(null));
        } else {
            GuildMessageChannel guildMessageChannel = getMessageChannelForPostTarget(target);
            log.debug("Send messageToSend towards post target {}.", target.getName());
            return channelService.sendMessageToSendToChannel(message, guildMessageChannel);
        }
    }

    @Override
    public List<CompletableFuture<Message>> editEmbedInPostTarget(Long messageId, MessageToSend message, PostTarget target)  {
        if(target.getDisabled()) {
            log.info("Post target {} has been disabled in server {} - not sending message.", target.getName(), target.getServerReference().getId());
            return Arrays.asList(CompletableFuture.completedFuture(null));
        } else {
            GuildMessageChannel guildMessageChannel = getMessageChannelForPostTarget(target);
            // always takes the first one, only applicable for this scenario
            String messageText = message.getMessages().get(0);
            if(StringUtils.isBlank(messageText)) {
                log.debug("Editing embeds of message {} in post target {}.", messageId, target.getName());
                return Arrays.asList(channelService.editEmbedMessageInAChannel(message.getEmbeds().get(0), guildMessageChannel, messageId));
            } else {
                log.debug("Editing message text and potentially text for message {} in post target {}.", messageId, target.getName());
                return Arrays.asList(channelService.editTextMessageInAChannel(messageText, message.getEmbeds().get(0), guildMessageChannel, messageId));
            }
        }
    }

    @Override
    public List<CompletableFuture<Message>> editOrCreatedInPostTarget(Long messageId, MessageToSend messageToSend, PostTarget target)  {
        if(target.getDisabled()) {
            log.info("Post target {} has been disabled in server {} - not sending message.", target.getName(), target.getServerReference().getId());
            return Arrays.asList(CompletableFuture.completedFuture(null));
        } else {
            List<CompletableFuture<Message>> futures = new ArrayList<>();
            GuildMessageChannel guildMessageChannel = getMessageChannelForPostTarget(target);
            CompletableFuture<Message> messageEditFuture = new CompletableFuture<>();
            futures.add(messageEditFuture);
            if (StringUtils.isBlank(messageToSend.getMessages().get(0).trim())) {
                channelService.retrieveMessageInChannel(guildMessageChannel, messageId).thenAccept(message -> {
                    log.debug("Editing existing message {} when upserting message embeds in channel {} in server {}.",
                            messageId, guildMessageChannel.getIdLong(), guildMessageChannel.getGuild().getId());
                    messageService.editMessage(message, messageToSend.getEmbeds().get(0))
                            .queue(messageEditFuture::complete, messageEditFuture::completeExceptionally);
                }).exceptionally(throwable -> {
                    log.debug("Creating new message when upserting message embeds for message {} in channel {} in server {}.",
                            messageId, guildMessageChannel.getIdLong(), guildMessageChannel.getGuild().getId());
                    sendEmbedInPostTarget(messageToSend, target).get(0)
                            .thenAccept(messageEditFuture::complete).exceptionally(innerThrowable -> {
                        log.error("Failed to send message to create a message.", innerThrowable);
                        messageEditFuture.completeExceptionally(innerThrowable);
                        return null;
                    });
                    return null;
                });
            } else {
                channelService.retrieveMessageInChannel(guildMessageChannel, messageId).thenAccept(message -> {
                    log.debug("Editing existing message {} when upserting message in channel {} in server {}.",
                            messageId, guildMessageChannel.getIdLong(), guildMessageChannel.getGuild().getId());
                    messageService.editMessage(message, messageToSend.getMessages().get(0), messageToSend.getEmbeds().get(0))
                            .queue(messageEditFuture::complete, messageEditFuture::completeExceptionally);
                }).exceptionally(throwable -> {
                    log.debug("Creating new message when trying to upsert a message {} in channel {} in server {}.",
                            messageId, guildMessageChannel.getIdLong(), guildMessageChannel.getGuild().getId());
                    sendEmbedInPostTarget(messageToSend, target).get(0)
                            .thenAccept(messageEditFuture::complete).exceptionally(innerThrowable -> {
                        log.error("Failed to send message to create a message.", innerThrowable);
                        messageEditFuture.completeExceptionally(innerThrowable);
                        return null;
                    });
                    return null;
                });
            }

            return futures;
        }
    }

    @Override
    public List<CompletableFuture<Message>> editOrCreatedInPostTarget(Long messageId, MessageToSend messageToSend, PostTargetEnum postTargetName, Long serverId)  {
        Optional<PostTarget> postTargetOptional = getPostTarget(postTargetName, serverId);
        if(!postTargetOptional.isPresent()) {
            return Arrays.asList(CompletableFuture.completedFuture(null));
        }
        return this.editOrCreatedInPostTarget(messageId, messageToSend, postTargetOptional.get());
    }

    @Override
    public void throwIfPostTargetIsNotDefined(PostTargetEnum name, Long serverId) {
        Optional<PostTarget> postTargetOptional = getPostTarget(name, serverId);
        if(!postTargetOptional.isPresent()) {
            throw new PostTargetNotValidException(name.getKey(), defaultPostTargetManagementService.getDefaultPostTargetKeys());
        }
    }

    @Override
    public boolean postTargetDefinedInServer(PostTargetEnum name, Long serverId) {
        return postTargetManagement.postTargetExists(name.getKey(), serverId);
    }

    @Override
    public List<CompletableFuture<Message>> editEmbedInPostTarget(Long messageId, MessageToSend message, PostTargetEnum postTargetName, Long serverId)  {
        Optional<PostTarget> postTargetOptional = getPostTarget(postTargetName, serverId);
        if(!postTargetOptional.isPresent()) {
            return Arrays.asList(CompletableFuture.completedFuture(null));
        }
        return editEmbedInPostTarget(messageId, message, postTargetOptional.get());
    }

    private Optional<PostTarget> getPostTarget(PostTargetEnum postTargetEnum, Long serverId) {
        return postTargetManagement.getPostTargetOptional(postTargetEnum.getKey(), serverId);
    }

    @Override
    public boolean validPostTarget(String name) {
        List<String> possiblePostTargets = defaultPostTargetManagementService.getDefaultPostTargetKeys();
        return possiblePostTargets.contains(name);
    }

    @Override
    public void validatePostTarget(PostTargetEnum name, Long serverId) {
        if(!postTargetUsableInServer(name, serverId)) {
            throw new PostTargetNotUsableException(name.getKey());
        }
    }

    @Override
    public boolean postTargetUsableInServer(PostTargetEnum name, Long serverId) {
        Optional<PostTarget> postTargetOptional = getPostTarget(name, serverId);
        return postTargetOptional.isPresent() && !postTargetOptional.get().getDisabled();
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

    @Override
    public void disablePostTarget(String name, Long serverId) {
        PostTarget postTarget = postTargetManagement.getPostTarget(name, serverId);
        postTarget.setDisabled(true);
    }

    @Override
    public void enablePostTarget(String name, Long serverId) {
        PostTarget postTarget = postTargetManagement.getPostTarget(name, serverId);
        postTarget.setDisabled(false);
    }
}
