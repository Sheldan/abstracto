package dev.sheldan.abstracto.linkembed.service;

import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.GuildMemberMessageChannel;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.interaction.button.ButtonConfigModel;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.linkembed.config.LinkEmbedFeatureDefinition;
import dev.sheldan.abstracto.linkembed.config.LinkEmbedFeatureMode;
import dev.sheldan.abstracto.linkembed.model.template.MessageEmbedDeleteButtonPayload;
import dev.sheldan.abstracto.linkembed.model.template.MessageEmbeddedModel;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.linkembed.model.MessageEmbedLink;
import dev.sheldan.abstracto.linkembed.model.database.EmbeddedMessage;
import dev.sheldan.abstracto.linkembed.service.management.MessageEmbedPostManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MessageEmbedServiceBean implements MessageEmbedService {

    private final Pattern messageRegex = Pattern.compile("(?<whole>(?:https?://)?(?:\\w+\\.)?discord(?:app)?\\.com/channels/(?<server>\\d+)/(?<channel>\\d+)/(?<message>\\d+)(?:.*?))+", Pattern.CASE_INSENSITIVE);

    public static final String MESSAGE_EMBED_TEMPLATE = "message_embed";
    public static final String REMOVAL_EMOTE = "removeEmbed";

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private UserService userService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MessageEmbedServiceBean self;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private MessageEmbedPostManagementService messageEmbedPostManagementService;

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ComponentService componentServiceBean;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Value("${abstracto.feature.linkEmbed.removalDays}")
    private Long embedRemovalDays;

    public static final String MESSAGE_EMBED_DELETE_ORIGIN = "messageEmbedDelete";

    @Override
    public List<MessageEmbedLink> getLinksInMessage(String message) {
        List<MessageEmbedLink> links = new ArrayList<>();
        Matcher matcher = messageRegex.matcher(message);
        while(matcher.find()) {
            String serverId = matcher.group("server");
            String channelId = matcher.group("channel");
            String messageId = matcher.group("message");
            String wholeLink = matcher.group("whole");
            Long serverIdLong = Long.parseLong(serverId);
            Long channelIdLong = Long.parseLong(channelId);
            Long messageIdLong = Long.parseLong(messageId);
            MessageEmbedLink messageEmbedLink = MessageEmbedLink
                    .builder()
                    .serverId(serverIdLong)
                    .channelId(channelIdLong)
                    .messageId(messageIdLong)
                    .wholeUrl(wholeLink)
                    .build();
            links.add(messageEmbedLink);
        }
        return links;
    }

    @Override
    public void embedLinks(List<MessageEmbedLink> linksToEmbed, GuildMessageChannel target, Long userEmbeddingUserInServerId, GuildMemberMessageChannel executionContext) {
        linksToEmbed.forEach(messageEmbedLink ->
            messageCache.getMessageFromCache(messageEmbedLink.getServerId(), messageEmbedLink.getChannelId(), messageEmbedLink.getMessageId())
                    .thenAccept(cachedMessage -> self.embedLink(cachedMessage, target, userEmbeddingUserInServerId, executionContext)
                    ).exceptionally(throwable -> {
                log.error("Message embedding from cache failed for message {}.", messageEmbedLink.getMessageId(), throwable);
                return null;
            })
        );
    }

    @Override
    @Transactional
    public CompletableFuture<Void> embedLink(CachedMessage cachedMessage, GuildMessageChannel target, Long userEmbeddingUserInServerId, GuildMemberMessageChannel executionContext) {
        boolean deletionButtonEnabled = featureModeService.featureModeActive(LinkEmbedFeatureDefinition.LINK_EMBEDS, target.getGuild(), LinkEmbedFeatureMode.DELETE_BUTTON);
        return buildTemplateParameter(executionContext, cachedMessage, deletionButtonEnabled).thenCompose(messageEmbeddedModel ->
            self.sendEmbeddingMessage(cachedMessage, target, userEmbeddingUserInServerId, messageEmbeddedModel, deletionButtonEnabled)
        );
    }

    @Override
    public CompletableFuture<Void> embedLink(CachedMessage cachedMessage, GuildMessageChannel target, Long userEmbeddingUserInServerId, GuildMemberMessageChannel executionContext, CommandInteraction interaction) {
        boolean deletionButtonEnabled = featureModeService.featureModeActive(LinkEmbedFeatureDefinition.LINK_EMBEDS, target.getGuild(), LinkEmbedFeatureMode.DELETE_BUTTON);
        return buildTemplateParameter(executionContext, cachedMessage, deletionButtonEnabled).thenCompose(messageEmbeddedModel ->
            self.sendEmbeddingMessageToInteraction(cachedMessage, target, userEmbeddingUserInServerId, messageEmbeddedModel, deletionButtonEnabled, interaction)
        );
    }

    @Override
    public CompletableFuture<Void> cleanUpOldMessageEmbeds() {
        Instant oldestDate = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(embedRemovalDays, ChronoUnit.DAYS);
        List<EmbeddedMessage> embeddedMessages = messageEmbedPostManagementService.getEmbeddedMessagesOlderThan(oldestDate);
        if(embeddedMessages.isEmpty()) {
            log.info("No embedded messages to clean up.");
            return CompletableFuture.completedFuture(null);
        }
        log.info("Cleaning up {} embedded embeddedMessages", embeddedMessages.size());
        List<ServerChannelMessage> reactionChannelMessages = embeddedMessages.stream()
                .filter(embeddedMessage -> embeddedMessage.getDeletionComponentId() == null)
                .map(this::convertEmbedMessageToServerChannelMessage)
        .collect(Collectors.toList());

        List<ServerChannelMessage> buttonChannelMessages = embeddedMessages.stream()
                .filter(embeddedMessage -> embeddedMessage.getDeletionComponentId() != null)
                .map(this::convertEmbedMessageToServerChannelMessage)
                .collect(Collectors.toList());
        List<Long> embeddedMessagesHandled = embeddedMessages
                .stream()
                .map(EmbeddedMessage::getEmbeddingMessageId)
                .collect(Collectors.toList());
        List<String> componentPayloadsToDelete = embeddedMessages
                .stream()
                .map(EmbeddedMessage::getDeletionComponentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<CompletableFuture<Message>> reactionMessageFutures = messageService.retrieveMessages(reactionChannelMessages);
        List<CompletableFuture<Message>> buttonMessageFutures = messageService.retrieveMessages(buttonChannelMessages);
        CompletableFutureList<Message> reactionFutureList = new CompletableFutureList<>(reactionMessageFutures);
        CompletableFutureList<Message> buttonFutureList = new CompletableFutureList<>(buttonMessageFutures);
        return reactionFutureList.getMainFuture()
                .handle((unused, throwable) -> {
                    if(throwable != null) {
                        log.warn("Embedded messages reaction message loading failed.", throwable);
                    }
                    return self.removeReactions(reactionFutureList.getObjects());
                })
                .thenCompose(Function.identity())
                .thenCompose(unused -> buttonFutureList.getMainFuture())
                .handle((unused, throwable) -> {
                    if(throwable != null) {
                        log.warn("Embedded messages button message loading failed.", throwable);
                    }
                    return self.removeButtons(buttonFutureList.getObjects());
                })
                // deleting the messages from db regardless of exceptions, at most the reaction remains
                .thenCompose(Function.identity())
                .whenComplete((unused, throwable) -> {
                    if(throwable != null) {
                        log.warn("Embedded message button clearing failed.", throwable);
                    }
                    self.deleteEmbeddedMessages(embeddedMessagesHandled, componentPayloadsToDelete);
                })
                .exceptionally(throwable -> {
                    log.error("Failed to clean up embedded messages.", throwable);
                    return null;
                });
    }

    public CompletableFuture<Void> removeButtons(List<Message> messages) {
        List<CompletableFuture<Void>> removalFutures = new ArrayList<>();
        messages.forEach(message -> removalFutures.add(componentServiceBean.clearButtons(message)));
        return FutureUtils.toSingleFutureGeneric(removalFutures);
    }

    private ServerChannelMessage convertEmbedMessageToServerChannelMessage(EmbeddedMessage embeddedMessage) {
        return ServerChannelMessage
                .builder()
                .serverId(embeddedMessage.getEmbeddingServer().getId())
                .channelId(embeddedMessage.getEmbeddingChannel().getId())
                .messageId(embeddedMessage.getEmbeddingMessageId())
                .build();
    }

    @Transactional
    public CompletableFuture<Void> removeReactions(List<Message> allMessages) {
        List<CompletableFuture<Void>> removalFutures = new ArrayList<>();
        Map<Long, List<Message>> groupedPerServer = allMessages
                .stream()
                .collect(Collectors.groupingBy(message -> message.getGuild().getIdLong()));
        groupedPerServer.forEach((serverId, serverMessages) -> {
            // we assume the emote remained the same
            CompletableFutureList<Void> removalFuture = reactionService.removeReactionFromMessagesWithFutureWithFutureList(serverMessages, REMOVAL_EMOTE);
            removalFutures.add(removalFuture.getMainFuture());
        });
        return FutureUtils.toSingleFutureGeneric(removalFutures);
    }

    @Transactional
    public void deleteEmbeddedMessages(List<Long> embeddedMessagesToDelete, List<String> componentPayloadsToDelete) {
        messageEmbedPostManagementService.deleteEmbeddedMessagesViaId(embeddedMessagesToDelete);
        componentPayloadManagementService.deletePayloads(componentPayloadsToDelete);
    }

    @Transactional
    public CompletableFuture<Void> sendEmbeddingMessage(CachedMessage cachedMessage, GuildMessageChannel target,
                                                        Long userEmbeddingUserInServerId, MessageEmbeddedModel messageEmbeddedModel, Boolean deletionButtonEnabled) {
        MessageToSend embed = templateService.renderEmbedTemplate(MESSAGE_EMBED_TEMPLATE, messageEmbeddedModel, target.getGuild().getIdLong());
        List<CompletableFuture<Message>> completableFutures = channelService.sendMessageToSendToChannel(embed, target);
        return postProcessLinkEmbed(cachedMessage, userEmbeddingUserInServerId, messageEmbeddedModel, deletionButtonEnabled, completableFutures);
    }

    private CompletableFuture<Void> postProcessLinkEmbed(CachedMessage cachedMessage, Long userEmbeddingUserInServerId, MessageEmbeddedModel messageEmbeddedModel, Boolean deletionButtonEnabled, List<CompletableFuture<Message>> completableFutures) {
        AUserInAServer cause = userInServerManagementService.loadOrCreateUser(userEmbeddingUserInServerId);
        Long embeddingUserId = cause.getUserReference().getId();
        log.debug("Embedding message {} from channel {} from server {}, because of user {}", cachedMessage.getMessageId(),
                cachedMessage.getChannelId(), cachedMessage.getServerId(), embeddingUserId);
        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenCompose(aVoid -> {
            Message createdMessage = completableFutures.get(0).join();
            return self.addDeletionPossibility(cachedMessage, messageEmbeddedModel, createdMessage, embeddingUserId, deletionButtonEnabled);
        });
    }

    @Transactional
    public CompletableFuture<Void> sendEmbeddingMessageToInteraction(CachedMessage cachedMessage, GuildMessageChannel target,
                                                        Long userEmbeddingUserInServerId, MessageEmbeddedModel messageEmbeddedModel, Boolean deletionButtonEnabled, CommandInteraction interaction) {
        MessageToSend embed = templateService.renderEmbedTemplate(MESSAGE_EMBED_TEMPLATE, messageEmbeddedModel, target.getGuild().getIdLong());
        List<CompletableFuture<Message>> completableFutures = interactionService.sendMessageToInteraction(embed, interaction.getHook());
        return postProcessLinkEmbed(cachedMessage, userEmbeddingUserInServerId, messageEmbeddedModel, deletionButtonEnabled, completableFutures);
    }

    @Transactional
    public CompletableFuture<Void> addDeletionPossibility(CachedMessage cachedMessage, MessageEmbeddedModel messageEmbeddedModel,
                                                          Message createdMessage, Long embeddingUserId, Boolean deletionButtonEnabled) {
        Long serverId = createdMessage.getGuild().getIdLong();
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(serverId, embeddingUserId);
        Long embeddingUserInServerId = aUserInAServer.getUserInServerId();
        if(deletionButtonEnabled) {
            ButtonConfigModel buttonConfigModel = messageEmbeddedModel.getButtonConfigModel();
            buttonConfigModel.setButtonPayload(getButtonPayload(createdMessage, cachedMessage, embeddingUserId));
            buttonConfigModel.setOrigin(MESSAGE_EMBED_DELETE_ORIGIN);
            buttonConfigModel.setPayloadType(MessageEmbedDeleteButtonPayload.class);
            AServer server = serverManagementService.loadServer(serverId);
            componentPayloadManagementService.createButtonPayload(buttonConfigModel, server);
            self.loadUserAndPersistMessage(cachedMessage, embeddingUserInServerId, createdMessage, messageEmbeddedModel.getButtonConfigModel().getButtonId());
            return CompletableFuture.completedFuture(null);
        } else {
            return reactionService.addReactionToMessageAsync(REMOVAL_EMOTE, cachedMessage.getServerId(), createdMessage).thenAccept(aVoid1 ->
                self.loadUserAndPersistMessage(cachedMessage, embeddingUserInServerId, createdMessage, null)
            );
        }
    }

    public MessageEmbedDeleteButtonPayload getButtonPayload(Message embeddingMessage, CachedMessage embeddedMessage, Long embeddingUserId){
        return MessageEmbedDeleteButtonPayload
                .builder()
                .embeddedMessageId(embeddedMessage.getMessageId())
                .embeddedChannelId(embeddedMessage.getChannelId())
                .embeddedServerId(embeddedMessage.getServerId())
                .embeddedUserId(embeddedMessage.getAuthor().getAuthorId())
                .embeddingMessageId(embeddingMessage.getIdLong())
                .embeddingChannelId(embeddingMessage.getChannel().getIdLong())
                .embeddingServerId(embeddingMessage.getGuild().getIdLong())
                .embeddingUserId(embeddingUserId)
                .build();
    }

    @Transactional
    public void loadUserAndPersistMessage(CachedMessage cachedMessage, Long embeddingUserId, Message createdMessage, String deletionButtonId) {
        AUserInAServer innerCause = userInServerManagementService.loadOrCreateUser(embeddingUserId);
        messageEmbedPostManagementService.createMessageEmbed(cachedMessage, createdMessage, innerCause, deletionButtonId);
    }

    private CompletableFuture<MessageEmbeddedModel> buildTemplateParameter(GuildMemberMessageChannel executionContext, CachedMessage embeddedMessage, Boolean deletionButtonEnabled) {
        return userService.retrieveUserForId(embeddedMessage.getAuthor().getAuthorId()).thenApply(authorUser ->
            self.loadMessageEmbedModel(executionContext, embeddedMessage, authorUser, deletionButtonEnabled)
        ).exceptionally(throwable -> {
            log.warn("Failed to retrieve author for user {}.", embeddedMessage.getAuthor().getAuthorId(), throwable);
            return self.loadMessageEmbedModel(executionContext, embeddedMessage, null, deletionButtonEnabled);
        });
    }

    @Transactional
    public MessageEmbeddedModel loadMessageEmbedModel(GuildMemberMessageChannel executionContext, CachedMessage embeddedMessage, User userAuthor, Boolean deletionButtonEnabled) {
        Optional<GuildMessageChannel> textChannelFromServer = channelService.getMessageChannelFromServerOptional(embeddedMessage.getServerId(), embeddedMessage.getChannelId());
        GuildMessageChannel sourceChannel = textChannelFromServer.orElse(null);
        ButtonConfigModel buttonConfigModel = ButtonConfigModel
                .builder()
                .buttonId(deletionButtonEnabled ? componentServiceBean.generateComponentId() : null)
                .build();
        Long referencedMessageId = null;
        Boolean shouldMentionReferencedAuthor = false;
        if(executionContext.getMessage() != null) {
            referencedMessageId = executionContext.getMessage().getReferencedMessage() != null ? executionContext.getMessage().getReferencedMessage().getIdLong() : null;
            shouldMentionReferencedAuthor = shouldMentionReferencedAuthor(executionContext.getMessage());
        }
        return MessageEmbeddedModel
                .builder()
                .member(executionContext.getMember())
                .author(userAuthor)
                .sourceChannel(sourceChannel)
                .embeddingUser(executionContext.getMember())
                .messageChannel(executionContext.getGuildChannel())
                .guild(executionContext.getGuild())
                .useButton(deletionButtonEnabled)
                .embeddedMessage(embeddedMessage)
                .referencedMessageId(referencedMessageId)
                .mentionsReferencedMessage(shouldMentionReferencedAuthor)
                .buttonConfigModel(buttonConfigModel)
                .build();
    }

    private Boolean shouldMentionReferencedAuthor(Message message) {
        if(message.getReferencedMessage() != null) {
            return message.getMentions().getMentions(Message.MentionType.USER).contains(message.getReferencedMessage().getAuthor());
        }
        return false;
    }
}
