package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.exception.PostTargetNotUsableException;
import dev.sheldan.abstracto.core.exception.PostTargetNotValidException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultPostTargetManagementService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import java.util.function.BiConsumer;
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

    public static final String POSTTARGET_CHANNEL_GROUP_TYPE = "posttarget";
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

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;

    @Override
    public List<CompletableFuture<Message>> sendTextInPostTarget(String text, PostTarget target) {
        if (target.getDisabled()) {
            log.info("Post target {} has been disabled in server {} - not sending message.", target.getName(), target.getServerReference().getId());
            return List.of(CompletableFuture.completedFuture(null));
        } else {
            log.debug("Sending text to post target {}.", target.getName());
            List<CompletableFuture<Message>> futures = new ArrayList<>();
            CompletableFuture<Message> mainTargetFuture = channelService.sendTextToAChannel(text, target.getChannelReference());
            futures.add(mainTargetFuture);
            BiConsumer<AChannel, List<CompletableFuture<Message>>> sendToChannel = (AChannel channel, List<CompletableFuture<Message>> list)
                -> list.add(channelService.sendTextToAChannel(text, channel));
            executeForSingleMessages(target, sendToChannel, futures);
            return futures;
        }
    }

    private void executeForSingleMessages(PostTarget target, BiConsumer<AChannel, List<CompletableFuture<Message>>> sendToChannel, List<CompletableFuture<Message>> futures) {
        Optional<AChannelGroup> channelGroupOptional = channelGroupManagementService.findByNameAndServerAndTypeOptional(target.getName(), target.getServerReference(),
            POSTTARGET_CHANNEL_GROUP_TYPE);
        channelGroupOptional.ifPresent(channelGroup -> {
            if(channelGroup.getEnabled()) {
                channelGroup.getChannels().forEach(aChannel -> {
                    log.debug("Sending message also to channel due to channel group {}.", aChannel.getId());
                    sendToChannel.accept(aChannel, futures);
                });
            }
        });
    }

    private void executeForMultipleMessages(PostTarget target, BiConsumer<AChannel, List<List<CompletableFuture<Message>>>> sendToChannel, List<List<CompletableFuture<Message>>> futures) {
        Optional<AChannelGroup> channelGroupOptional = channelGroupManagementService.findByNameAndServerAndTypeOptional(target.getName(), target.getServerReference(),
            POSTTARGET_CHANNEL_GROUP_TYPE);
        channelGroupOptional.ifPresent(channelGroup -> {
            if(channelGroup.getEnabled()) {
                channelGroup.getChannels().forEach(aChannel -> {
                    log.debug("Sending message also to channel due to channel group {}.", aChannel.getId());
                    sendToChannel.accept(aChannel, futures);
                });
            }
        });
    }

    @Override
    public List<CompletableFuture<Message>> sendEmbedInPostTarget(MessageEmbed embed, PostTarget target) {
        if (target.getDisabled()) {
            log.info("Post target {} has been disabled in server {} - not sending message.", target.getName(), target.getServerReference().getId());
            return List.of(CompletableFuture.completedFuture(null));
        } else {
            log.debug("Sending message embed to post target {}.", target.getName());
            List<CompletableFuture<Message>> futures = new ArrayList<>();
            CompletableFuture<Message> mainTargetFutures = getMessageChannelForPostTarget(target)
                .map(channel -> channelService.sendEmbedToChannel(embed, channel))
                .orElse(CompletableFuture.completedFuture(null));
            futures.add(mainTargetFutures);
            BiConsumer<AChannel, List<CompletableFuture<Message>>> sendToChannel = (AChannel channel, List<CompletableFuture<Message>> list)
                -> list.add(getMessageChannelForPostTarget(target)
                .map(innerChannel -> channelService.sendEmbedToChannel(embed, innerChannel))
                .orElse(CompletableFuture.completedFuture(null)));
            executeForSingleMessages(target, sendToChannel, futures);
            return futures;
        }
    }

    private Optional<GuildMessageChannel> getMessageChannelForPostTarget(PostTarget target) {
        Guild guild = botService.getInstance().getGuildById(target.getServerReference().getId());
        if (guild != null) {
            GuildChannel guildChannelById = guild.getGuildChannelById(target.getChannelReference().getId());
            if (guildChannelById instanceof GuildMessageChannel) {
                return Optional.of((GuildMessageChannel) guildChannelById);
            } else {
                log.error("Incorrect post target configuration (it is not a message channel): {} points to {} on server {}", target.getName(),
                        target.getChannelReference().getId(), target.getServerReference().getId());
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<CompletableFuture<Message>> sendTextInPostTarget(String text, PostTargetEnum postTargetEnum, Long serverId) {
        Optional<PostTarget> postTargetOptional = getPostTarget(postTargetEnum, serverId);
        if (postTargetOptional.isEmpty()) {
            return List.of(CompletableFuture.completedFuture(null));
        }
        return this.sendTextInPostTarget(text, postTargetOptional.get());
    }

    @Override
    public List<CompletableFuture<Message>> sendEmbedInPostTarget(MessageEmbed embed, PostTargetEnum postTargetName, Long serverId) {
        Optional<PostTarget> postTargetOptional = getPostTarget(postTargetName, serverId);
        if (postTargetOptional.isEmpty()) {
            return List.of(CompletableFuture.completedFuture(null));
        }
        return this.sendEmbedInPostTarget(embed, postTargetOptional.get());
    }

    @Override
    public List<CompletableFuture<Message>> sendMessageInPostTarget(Message message, PostTargetEnum postTargetName, Long serverId) {
        Optional<PostTarget> postTargetOptional = getPostTarget(postTargetName, serverId);
        if (postTargetOptional.isEmpty()) {
            return List.of(CompletableFuture.completedFuture(null));
        }
        return sendMessageInPostTarget(message, postTargetOptional.get());
    }

    @Override
    public List<CompletableFuture<Message>> sendMessageInPostTarget(Message message, PostTarget target) {
        if (target.getDisabled()) {
            log.info("Post target {} has been disabled in server {} - not sending message.", target.getName(), target.getServerReference().getId());
            return List.of(CompletableFuture.completedFuture(null));
        } else {
            log.debug("Send message {} towards post target {}.", message.getId(), target.getName());
            List<CompletableFuture<Message>> futures = new ArrayList<>();
            CompletableFuture<Message> mainFuture = channelService.sendMessageToAChannel(message, target.getChannelReference());
            futures.add(mainFuture);
            BiConsumer<AChannel, List<CompletableFuture<Message>>> sendToChannel = (AChannel channel, List<CompletableFuture<Message>> list)
                -> list.add(channelService.sendMessageToAChannel(message, target.getChannelReference()));
            executeForSingleMessages(target, sendToChannel, futures);
            return futures;
        }
    }

    @Override
    public List<List<CompletableFuture<Message>>> sendEmbedInPostTarget(MessageToSend message, PostTargetEnum postTargetName, Long serverId) {
        Optional<PostTarget> postTargetOptional = getPostTarget(postTargetName, serverId);
        if (postTargetOptional.isEmpty()) {
            return List.of(List.of(CompletableFuture.completedFuture(null)));
        }
        return this.sendEmbedInPostTarget(message, postTargetOptional.get());
    }

    @Override
    public List<List<CompletableFuture<Message>>> sendEmbedInPostTarget(List<MessageToSend> messages, PostTargetEnum postTargetName, Long serverId) {
        Optional<PostTarget> postTargetOptional = getPostTarget(postTargetName, serverId);
        if (postTargetOptional.isEmpty()) {
            return List.of(List.of(CompletableFuture.completedFuture(null)));
        }
        PostTarget target = postTargetOptional.get();
        if (target.getDisabled()) {
            log.info("Post target {} has been disabled in server {} - not sending message.", target.getName(), target.getServerReference().getId());
            return List.of(List.of(CompletableFuture.completedFuture(null)));
        } else {
            log.debug("Send messageToSend towards post target {}.", target.getName());
            List<CompletableFuture<Message>> mainTargetFutures;
            if(!messages.isEmpty()) {
                // the primary post target channel gets the first message to send
                mainTargetFutures = getMessageChannelForPostTarget(target)
                    .map(channel -> channelService.sendMessageToSendToChannel(messages.get(0), channel))
                    .orElse(Arrays.asList(CompletableFuture.completedFuture(null)));
            } else {
                mainTargetFutures = new ArrayList<>();
                mainTargetFutures.add(CompletableFuture.completedFuture(null));
            }
            List<List<CompletableFuture<Message>>> futures = new ArrayList<>();
            futures.add(mainTargetFutures);
            Optional<AChannelGroup> channelGroupOptional = channelGroupManagementService.findByNameAndServerAndTypeOptional(target.getName(), target.getServerReference(),
                POSTTARGET_CHANNEL_GROUP_TYPE);
            // the rest of the additional post target channels get the further message to sends
            // as appropriate, if we are at the end of the available ones and there are still channels left, we just keep the same
            channelGroupOptional.ifPresent(channelGroup -> {
                if(channelGroup.getEnabled()) {
                    for (int i = 0; i <  channelGroup.getChannels().size(); i++) {
                        AChannel aChannel = channelGroup.getChannels().get(i);
                        log.debug("Sending message also to channel due to channel group {}.", aChannel.getId());
                        Optional<GuildMessageChannel> messageOptional =
                            channelService.getGuildMessageChannelFromAChannelOptional(aChannel);
                        List<CompletableFuture<Message>> innerMessageFutures;
                        if(messageOptional.isPresent()) {
                            innerMessageFutures =
                                channelService.sendMessageToSendToChannel(messages.get(Math.min(messages.size() - 1, i + 1)), messageOptional.get());
                        } else {
                            innerMessageFutures = Arrays.asList(CompletableFuture.completedFuture(null));
                        }
                        futures.add(innerMessageFutures);
                    }
                }
            });
            return futures;
        }
    }

    @Override
    public List<List<CompletableFuture<Message>>> sendEmbedInPostTarget(MessageToSend message, PostTarget target) {
        if (target.getDisabled()) {
            log.info("Post target {} has been disabled in server {} - not sending message.", target.getName(), target.getServerReference().getId());
            return List.of(List.of(CompletableFuture.completedFuture(null)));
        } else {
            log.debug("Send messageToSend towards post target {}.", target.getName());
            List<CompletableFuture<Message>> mainTargetFutures = getMessageChannelForPostTarget(target)
                .map(channel -> channelService.sendMessageToSendToChannel(message, channel))
                .orElse(Arrays.asList(CompletableFuture.completedFuture(null)));
            List<List<CompletableFuture<Message>>> futures = new ArrayList<>();
            futures.add(mainTargetFutures);
            BiConsumer<AChannel, List<List<CompletableFuture<Message>>>> sendToChannel = (AChannel channel, List<List<CompletableFuture<Message>>> list)
                -> list.add(channelService.getGuildMessageChannelFromAChannelOptional(channel)
                .map(innerChannel -> channelService.sendMessageToSendToChannel(message, innerChannel))
                .orElse(Arrays.asList(CompletableFuture.completedFuture(null))));
            executeForMultipleMessages(target, sendToChannel, futures);
            return futures;
        }
    }

    @Override
    public List<CompletableFuture<Message>> editEmbedInPostTarget(Long messageId, MessageToSend message, PostTarget target) {
        if (target.getDisabled()) {
            log.info("Post target {} has been disabled in server {} - not sending message.", target.getName(), target.getServerReference().getId());
            return Arrays.asList(CompletableFuture.completedFuture(null));
        } else {
            return getMessageChannelForPostTarget(target).map(messageChannel -> {
                // always takes the first one, only applicable for this scenario
                String messageText = message.getMessages().get(0);
                if (StringUtils.isBlank(messageText)) {
                    log.debug("Editing embeds of message {} in post target {}.", messageId, target.getName());
                    return Arrays.asList(channelService.editEmbedMessageInAChannel(message.getEmbeds().get(0), messageChannel, messageId));
                } else {
                    log.debug("Editing message text and potentially text for message {} in post target {}.", messageId, target.getName());
                    return Arrays.asList(channelService.editTextMessageInAChannel(messageText, message.getEmbeds().get(0), messageChannel, messageId));
                }
            }).orElse(Arrays.asList(CompletableFuture.completedFuture(null)));
        }
    }

    @Override
    public List<CompletableFuture<Message>> editOrCreatedInPostTarget(Long messageId, MessageToSend messageToSend, PostTarget target) {
        if (target.getDisabled()) {
            log.info("Post target {} has been disabled in server {} - not sending message.", target.getName(), target.getServerReference().getId());
            return Arrays.asList(CompletableFuture.completedFuture(null));
        } else {
            List<CompletableFuture<Message>> futures = new ArrayList<>();
            return getMessageChannelForPostTarget(target).map(messageChannel -> {
                CompletableFuture<Message> messageEditFuture = new CompletableFuture<>();
                futures.add(messageEditFuture);
                if(messageToSend.getUseComponentsV2()) {
                    channelService.retrieveMessageInChannel(messageChannel, messageId).thenAccept(message -> {
                        log.debug("Editing existing message {} when upserting message embeds in channel {} in server {}.",
                            messageId, messageChannel.getIdLong(), messageChannel.getGuild().getId());
                        channelService.editMessageInAChannelFuture(messageToSend, messageChannel, messageId)
                            .thenAccept(messageEditFuture::complete)
                            .exceptionally(throwable -> {
                                messageEditFuture.completeExceptionally(throwable);
                                return null;
                            });
                    }).exceptionally(throwable -> {
                        log.debug("Creating new message when upserting message embeds for message {} in channel {} in server {}.",
                            messageId, messageChannel.getIdLong(), messageChannel.getGuild().getId());
                        List<CompletableFuture<Message>> messageFutures = channelService.sendMessageToSendToChannel(messageToSend, messageChannel);
                        FutureUtils.toSingleFutureGeneric(messageFutures)
                                .thenAccept(unused -> messageEditFuture.complete(futures.get(0).join()))
                                    .exceptionally(innerThrowable -> {
                                        log.error("Failed to send message to create a message.", innerThrowable);
                                        messageEditFuture.completeExceptionally(innerThrowable);
                                        return null;
                                    });
                        return null;
                    });
                } else {
                    if (StringUtils.isBlank(messageToSend.getMessages().get(0).trim())) {
                        channelService.retrieveMessageInChannel(messageChannel, messageId).thenAccept(message -> {
                            log.debug("Editing existing message {} when upserting message embeds in channel {} in server {}.",
                                messageId, messageChannel.getIdLong(), messageChannel.getGuild().getId());
                            messageService.editMessage(message, messageToSend.getEmbeds().get(0))
                                .queue(messageEditFuture::complete, messageEditFuture::completeExceptionally);
                        }).exceptionally(throwable -> {
                            log.debug("Creating new message when upserting message embeds for message {} in channel {} in server {}.",
                                messageId, messageChannel.getIdLong(), messageChannel.getGuild().getId());
                            sendEmbedInPostTarget(messageToSend, target).get(0).get(0)
                                .thenAccept(messageEditFuture::complete).exceptionally(innerThrowable -> {
                                    log.error("Failed to send message to create a message.", innerThrowable);
                                    messageEditFuture.completeExceptionally(innerThrowable);
                                    return null;
                                });
                            return null;
                        });
                    } else {
                        channelService.retrieveMessageInChannel(messageChannel, messageId).thenAccept(message -> {
                            log.debug("Editing existing message {} when upserting message in channel {} in server {}.",
                                messageId, messageChannel.getIdLong(), messageChannel.getGuild().getId());
                            messageService.editMessage(message, messageToSend.getMessages().get(0), messageToSend.getEmbeds().get(0))
                                .queue(messageEditFuture::complete, messageEditFuture::completeExceptionally);
                        }).exceptionally(throwable -> {
                            log.debug("Creating new message when trying to upsert a message {} in channel {} in server {}.",
                                messageId, messageChannel.getIdLong(), messageChannel.getGuild().getId());
                            sendEmbedInPostTarget(messageToSend, target).get(0).get(0)
                                .thenAccept(messageEditFuture::complete).exceptionally(innerThrowable -> {
                                    log.error("Failed to send message to create a message.", innerThrowable);
                                    messageEditFuture.completeExceptionally(innerThrowable);
                                    return null;
                                });
                            return null;
                        });
                    }
                }
                return futures;
            }).orElse(Arrays.asList(CompletableFuture.completedFuture(null)));
        }
    }

    @Override
    public List<CompletableFuture<Message>> editOrCreatedInPostTarget(Long messageId, MessageToSend messageToSend, PostTargetEnum postTargetName, Long serverId) {
        Optional<PostTarget> postTargetOptional = getPostTarget(postTargetName, serverId);
        if (postTargetOptional.isEmpty()) {
            return Arrays.asList(CompletableFuture.completedFuture(null));
        }
        return this.editOrCreatedInPostTarget(messageId, messageToSend, postTargetOptional.get());
    }

    @Override
    public void throwIfPostTargetIsNotDefined(PostTargetEnum targetEnum, Long serverId) {
        Optional<PostTarget> postTargetOptional = getPostTarget(targetEnum, serverId);
        if (postTargetOptional.isEmpty()) {
            throw new PostTargetNotValidException(targetEnum.getKey(), defaultPostTargetManagementService.getDefaultPostTargetKeys());
        }
    }

    @Override
    public boolean postTargetDefinedInServer(PostTargetEnum targetEnum, Long serverId) {
        return postTargetManagement.postTargetExists(targetEnum.getKey(), serverId);
    }

    @Override
    public List<CompletableFuture<Message>> editEmbedInPostTarget(Long messageId, MessageToSend message, PostTargetEnum postTargetName, Long serverId) {
        Optional<PostTarget> postTargetOptional = getPostTarget(postTargetName, serverId);
        if (postTargetOptional.isEmpty()) {
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
    public void validatePostTarget(PostTargetEnum targetEnum, Long serverId) {
        if (!postTargetUsableInServer(targetEnum, serverId)) {
            throw new PostTargetNotUsableException(targetEnum.getKey());
        }
    }

    @Override
    public boolean postTargetUsableInServer(PostTargetEnum targetEnum, Long serverId) {
        Optional<PostTarget> postTargetOptional = getPostTarget(targetEnum, serverId);
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
        if(allFeatureConfigs != null) {
            allFeatureConfigs.forEach(featureConfig -> {
                if (featureFlagService.isFeatureEnabled(featureConfig, server)) {
                    featureConfig.getRequiredPostTargets().forEach(postTargetEnum -> postTargets.add(postTargetEnum.getKey()));
                }
            });
        }
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

    @Override
    public Optional<GuildMessageChannel> getPostTargetChannel(PostTargetEnum postTargetEnum, Long serverId) {
        Optional<PostTarget> postTarget = getPostTarget(postTargetEnum, serverId);
        return postTarget.flatMap(target -> {
            if (target.getDisabled()) {
                return Optional.empty();
            } else {
                return getMessageChannelForPostTarget(target);
            }
        });
    }
}
