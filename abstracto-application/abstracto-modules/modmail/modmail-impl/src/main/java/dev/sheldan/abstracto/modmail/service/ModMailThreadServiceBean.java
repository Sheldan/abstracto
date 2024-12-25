package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.*;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.utils.SnowflakeUtils;
import dev.sheldan.abstracto.moderation.service.BanService;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureConfig;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailMode;
import dev.sheldan.abstracto.modmail.config.ModMailPostTargets;
import dev.sheldan.abstracto.modmail.exception.ModMailCategoryIdException;
import dev.sheldan.abstracto.modmail.exception.ModMailThreadChannelNotFound;
import dev.sheldan.abstracto.modmail.exception.ModMailThreadNotFoundException;
import dev.sheldan.abstracto.modmail.model.ClosingContext;
import dev.sheldan.abstracto.modmail.model.dto.ServiceChoicesPayload;
import dev.sheldan.abstracto.modmail.model.database.*;
import dev.sheldan.abstracto.modmail.model.listener.ModmailThreadCreatedSendMessageModel;
import dev.sheldan.abstracto.modmail.model.template.*;
import dev.sheldan.abstracto.modmail.service.management.ModMailMessageManagementService;
import dev.sheldan.abstracto.modmail.service.management.ModMailRoleManagementService;
import dev.sheldan.abstracto.modmail.service.management.ModMailSubscriberManagementService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import dev.sheldan.abstracto.modmail.validator.ModMailFeatureValidator;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.transaction.event.TransactionalEventListener;

import static dev.sheldan.abstracto.modmail.config.ModMailFeatureConfig.MOD_MAIL_CLOSING_TEXT_SYSTEM_CONFIG_KEY;

@Component
@Slf4j
public class ModMailThreadServiceBean implements ModMailThreadService {

    /**
     * The config key to use for the ID of the category to create {@link GuildMessageChannel} in
     */
    public static final String MODMAIL_CATEGORY = "modmailCategory";
    public static final String TEXT_CHANNEL_NAME_TEMPLATE_KEY = "modMail_channel_name";
    /**
     * The template key used for default mod mail exceptions
     */
    public static final String MODMAIL_CLOSE_PROGRESS_TEMPLATE_KEY = "modmail_closing_progress";
    public static final String MODMAIL_STAFF_MESSAGE_TEMPLATE_KEY = "modmail_staff_message";
    public static final String MODMAIL_THREAD_CREATED_TEMPLATE_KEY = "modmail_thread_created";

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ModMailMessageManagementService modMailMessageManagementService;

    @Autowired
    private ModMailMessageService modMailMessageService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private GuildService guildService;

    @Autowired
    private BanService banService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ModMailFeatureConfig modMailFeatureConfig;

    @Autowired
    private ModMailRoleManagementService modMailRoleManagementService;

    @Autowired
    private UndoActionService undoActionService;

    @Autowired
    private ModMailSubscriberManagementService modMailSubscriberManagementService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private ModMailFeatureValidator modMailFeatureValidator;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserService userService;

    @Autowired
    private ModMailThreadServiceBean self;

    @Autowired
    private MetricService metricService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadService componentPayloadService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public static final String MODMAIL_THREAD_METRIC = "modmail.threads";
    public static final String MODMAIL_MESSAGE_METRIC = "modmail.messges";
    public static final String ACTION = "action";
    public static final String MESSAGE_DIRECTION = "direction";
    private static final CounterMetric MODMAIL_THREAD_CREATED_COUNTER =
            CounterMetric
                    .builder()
                    .name(MODMAIL_THREAD_METRIC)
                    .tagList(Arrays.asList(MetricTag.getTag(ACTION, "created")))
                    .build();
    private static final CounterMetric MODMAIL_THREAD_CLOSED_COUNTER =
            CounterMetric
                    .builder()
                    .name(MODMAIL_THREAD_METRIC)
                    .tagList(Arrays.asList(MetricTag.getTag(ACTION, "closed")))
                    .build();
    private static final CounterMetric MDOMAIL_THREAD_MESSAGE_RECEIVED =
            CounterMetric
                    .builder().name(MODMAIL_MESSAGE_METRIC)
                    .tagList(Arrays.asList(MetricTag.getTag(MESSAGE_DIRECTION, "received")))
                    .build();
    private static final CounterMetric MDOMAIL_THREAD_MESSAGE_SENT =
            CounterMetric
                    .builder()
                    .name(MODMAIL_MESSAGE_METRIC)
                    .tagList(Arrays.asList(MetricTag.getTag(MESSAGE_DIRECTION, "sent")))
                    .build();

    public static final String MODMAIL_INITIAL_ORIGIN = "modmailInitial";

    public CompletableFuture<MessageChannel> createModMailThreadForUser(User user, Guild guild, Message initialMessage, boolean userInitiated, List<UndoActionInstance> undoActions,
                                                                        boolean appeal, ModmailThreadCreatedSendMessageModel createdSendMessageModel) {
        Long serverId = guild.getIdLong();
        AServer server = serverManagementService.loadServer(serverId);
        metricService.incrementCounter(MODMAIL_THREAD_CREATED_COUNTER);
        ModMailChannelNameModel model = ModMailChannelNameModel
                .builder()
                .serverId(serverId)
                .userId(user.getIdLong())
                .randomText(RandomStringUtils.randomAlphanumeric(25))
                .uuid(UUID.randomUUID().toString())
                .currentDate(Instant.now())
                .build();
        String channelName = templateService.renderTemplate(TEXT_CHANNEL_NAME_TEMPLATE_KEY, model, serverId);
        if (featureModeService.featureModeActive(ModMailFeatureDefinition.MOD_MAIL, serverId, ModMailMode.THREAD_CONTAINER)) {
            MessageToSend notificationMessageToSend = getModmailNotificationMessageToSend(user, null, serverId, false, appeal);
            Optional<GuildMessageChannel> modmailContainerOptional = postTargetService.getPostTargetChannel(ModMailPostTargets.MOD_MAIL_CONTAINER, serverId);
            if(modmailContainerOptional.isEmpty()) {
                throw new AbstractoRunTimeException("Modmail thread container not setup.");
            }
            GuildMessageChannel modmailContainer = modmailContainerOptional.get();
            Optional<TextChannel> textChannelOptional = channelService.getTextChannelFromServerOptional(serverId, modmailContainer.getIdLong());
            if(textChannelOptional.isEmpty()) {
                throw new AbstractoRunTimeException("Modmail thread container text channel not found.");
            }
            TextChannel textChannel = textChannelOptional.get();
            List<CompletableFuture<Message>> notificationMessage = channelService.sendMessageToSendToChannel(notificationMessageToSend, modmailContainer);
            return FutureUtils.toSingleFutureGeneric(notificationMessage)
                    .thenCompose(unused -> channelService.createThreadWithStarterMessage(textChannel, channelName, notificationMessage.get(0).join().getIdLong()))
                    .thenCompose(threadChannel -> {
                        undoActions.add(UndoActionInstance.getChannelDeleteAction(serverId, threadChannel.getIdLong()));
                        return self.performModMailThreadSetup(user, initialMessage, threadChannel, userInitiated, undoActions, appeal, createdSendMessageModel)
                                .thenCompose(unused -> CompletableFuture.completedFuture(threadChannel));
                    });
        } else {
            Long categoryId = configService.getLongValue(MODMAIL_CATEGORY, serverId);
            log.info("Creating modmail channel for user {} in category {} on server {}.", user.getId(), categoryId, serverId);
            CompletableFuture<TextChannel> textChannelFuture = channelService.createTextChannel(channelName, server, categoryId);
            return textChannelFuture.thenCompose(channel -> {
                undoActions.add(UndoActionInstance.getChannelDeleteAction(serverId, channel.getIdLong()));
                return self.performModMailThreadSetup(user, initialMessage, channel, userInitiated, undoActions, appeal, createdSendMessageModel)
                        .thenCompose(unused -> CompletableFuture.completedFuture(channel));
            });
        }
    }

    @Override
    public CompletableFuture<MessageChannel> createModMailThreadForUser(User user, Guild guild, Message initialMessage, boolean userInitiated,
                                                                        List<UndoActionInstance> undoActions, boolean appeal) {
        return createModMailThreadForUser(user, guild, initialMessage, userInitiated, undoActions, appeal, null);
    }

    @Transactional
    @Override
    public CompletableFuture<Void> sendContactNotification(User user, MessageChannel messageChannel, MessageChannel feedBackChannel) {
        ContactNotificationModel model = ContactNotificationModel
                .builder()
                .createdChannel(messageChannel)
                .userDisplay(UserDisplay.fromUser(user))
                .build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInMessageChannel(MODMAIL_THREAD_CREATED_TEMPLATE_KEY, model, feedBackChannel));
    }

    @Override
    public CompletableFuture<Void> sendContactNotification(User user, MessageChannel createdMessageChannel, InteractionHook interactionHook) {
        ContactNotificationModel model = ContactNotificationModel
                .builder()
                .createdChannel(createdMessageChannel)
                .userDisplay(UserDisplay.fromUser(user))
                .build();
        return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(MODMAIL_THREAD_CREATED_TEMPLATE_KEY, model, interactionHook));
    }

    /**
     * This method is responsible for creating the instance in the database, sending the header in the newly created text channel and forwarding the initial message
     * by the user (if any), after this is complete, this method executes the method to perform the mod mail notification.
     * @param user The {@link User} for which a {@link ModMailThread} is being created
     * @param initialMessage The {@link Message} which was sent by the user to open a thread, this is null, if the thread was opened via a command
     * @param channel The created {@link TextChannel} in which the mod mail thread is dealt with
     * @param userInitiated Whether the thread was initiated by a member
     * @param undoActions The list of actions to undo, in case an exception occurs
     * @param appeal Whether the modmail thread is for the purpose of an appeal
     * @param createdSendMessageModel The information which message should be sent after the modmail thread is completely created
     * @return A {@link CompletableFuture future} which completes when the setup is done
     */
    @Transactional
    public CompletableFuture<Void> performModMailThreadSetup(User user, Message initialMessage, GuildMessageChannel channel, boolean userInitiated, List<UndoActionInstance> undoActions,
                                                             boolean appeal, ModmailThreadCreatedSendMessageModel createdSendMessageModel) {
        log.info("Performing modmail thread setup for channel {} for user {} in server {}. It was initiated by a user: {}.", channel.getIdLong(), user.getId(), channel.getGuild().getId(), userInitiated);
        CompletableFuture<Void> headerFuture = sendModMailHeader(channel, user);
        CompletableFuture<Message> userReplyMessage;
        if(initialMessage != null){
            log.info("Sending initial message {} of user {} to modmail thread {}.", initialMessage.getId(), user.getId(), channel.getId());
            userReplyMessage = self.sendUserReply(channel, 0L, initialMessage, false);
        } else {
            log.info("No initial message to send.");
            userReplyMessage = CompletableFuture.completedFuture(null);
        }
        CompletableFuture notificationFuture;
        if (userInitiated) {
            notificationFuture = self.sendModMailNotification(user, channel, appeal);
        } else {
            notificationFuture = CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.allOf(headerFuture, notificationFuture, userReplyMessage).thenAccept(aVoid -> {
            undoActions.clear();
            self.setupModMailThreadInDB(initialMessage, channel, user, userReplyMessage.join(), appeal, createdSendMessageModel);
        });
    }

    @Transactional
    public void setupModMailThreadInDB(Message initialMessage, GuildMessageChannel channel, User user, Message sendMessage, boolean appeal,
                                       ModmailThreadCreatedSendMessageModel createdSendMessageModel) {
        log.info("Persisting info about modmail thread {} in database.", channel.getIdLong());
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(channel.getGuild().getIdLong(), user.getIdLong());
        ModMailThread thread = createThreadObject(channel, aUserInAServer, appeal);
        if(initialMessage != null) {
            log.debug("Adding initial message {} to modmail thread in channel {}.", initialMessage.getId(), channel.getId());
            modMailMessageManagementService.addMessageToThread(thread, null, sendMessage, initialMessage.getIdLong(), aUserInAServer, false, false);
        }
        if(createdSendMessageModel != null) {
            eventPublisher.publishEvent(createdSendMessageModel);
        }
    }

    /**
     * Sends the message containing the pings to notify the staff members to handle the opened {@link ModMailThread}
     * @param user The {@link FullUserInServer} which opened the thread
     * @param channel The created {@link GuildMessageChannel} in which the mod mail thread is dealt with
     * @param appeal Whether the modmail thread is for the purpose of an appeal
     * @return A {@link CompletableFuture future} which completes when the notification has been sent
     */
    @Transactional
    public CompletableFuture<Void> sendModMailNotification(User user, GuildMessageChannel channel, boolean appeal) {
        Long serverId = channel.getGuild().getIdLong();
        MessageToSend messageToSend = getModmailNotificationMessageToSend(user, channel, serverId, true, appeal);
        return FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(messageToSend, ModMailPostTargets.MOD_MAIL_PING, serverId));
    }

    private MessageToSend getModmailNotificationMessageToSend(User user, GuildMessageChannel channel, Long serverId, boolean pingRole, boolean appeal) {
        log.info("Sending modmail notification for new modmail thread about user {} in server {}.", user.getId(), serverId);
        AServer server = serverManagementService.loadServer(serverId);
        List<ModMailRole> rolesToPing;
        if(pingRole) {
            rolesToPing = modMailRoleManagementService.getRolesForServer(server);
        } else {
            rolesToPing = new ArrayList<>();
        }
        log.debug("Pinging {} roles to notify about modmail thread about user {} in server {}.", rolesToPing.size(), user.getId(), serverId);
        ModMailNotificationModel modMailNotificationModel = ModMailNotificationModel
                .builder()
                .userDisplay(UserDisplay.fromUser(user))
                .roles(rolesToPing)
                .appeal(appeal)
                .channel(channel)
                .build();
        return templateService.renderEmbedTemplate("modmail_notification_message", modMailNotificationModel, serverId);
    }

    /**
     * Creates the instance of the {@link ModMailThread} in the database.
     * @param channel The {@link GuildMessageChannel} in which the {@link ModMailThread} is being done
     * @param user The {@link AUserInAServer} which the thread is about
     * @param appeal Whether the modmail thread is for the purpose of an appeal
     * @return The created instance of {@link ModMailThread}
     */
    public ModMailThread createThreadObject(GuildMessageChannel channel, AUserInAServer user, boolean appeal) {
        log.info("Creating database objects related to modmail thread in channel {} and about user {} in server {}.", channel.getIdLong(), user.getUserReference().getId(), channel.getGuild().getId());
        boolean useThreads = featureModeService.featureModeActive(ModMailFeatureDefinition.MOD_MAIL, channel.getGuild().getIdLong(), ModMailMode.THREAD_CONTAINER);
        AChannel aChannel = channelManagementService.createChannel(channel.getIdLong(), useThreads ? AChannelType.PUBLIC_THREAD : AChannelType.TEXT, user.getServerReference());
        log.info("Creating mod mail thread in channel {} with db channel {}", channel.getIdLong(), aChannel.getId());
        return modMailThreadManagementService.createModMailThread(user, aChannel, appeal);
    }

    @Override
    public void setModMailCategoryTo(Guild guild, Long categoryId) {
        log.info("Trying to set modmail category to {} in guild {}.", categoryId, guild.getId());
        FeatureValidationResult result = FeatureValidationResult
                .builder()
                .build();
        modMailFeatureValidator.validateModMailCategory(result, guild, categoryId);
        if(!result.getValidationResult()) {
            throw new ModMailCategoryIdException(categoryId);
        }
        configService.setLongValue(MODMAIL_CATEGORY, guild.getIdLong(), categoryId);
    }

    @Override
    public void createModMailPrompt(AUser user, Message initialMessage) {
        List<AServer> servers = new ArrayList<>();
        List<Guild> mutualServers = initialMessage.getJDA().getMutualGuilds(initialMessage.getAuthor());
        mutualServers.forEach(guild -> {
            AServer server = serverManagementService.loadServer(guild);
            servers.add(server);
        });

        if(servers.isEmpty()) {
            log.warn("User {} which was not known in any of the servers tried to contact the bot.", user.getId());
            return;
        }

        log.info("There are {} shared servers between user and the bot.", servers.size());
        List<ServerChoice> availableGuilds = new ArrayList<>();
        Set<Long> alreadyConsideredServers = new HashSet<>();
        for (AServer server : servers) {
            // only take the servers in which mod mail is actually enabled, would not make much sense to make the
            // other servers available
            boolean possibleForModmail = featureFlagService.isFeatureEnabled(modMailFeatureConfig, server);
            if (possibleForModmail) {
                Guild guild = guildService.getGuildById(server.getId());
                ServerChoice serverChoice = ServerChoice
                        .builder()
                        .serverId(guild.getIdLong())
                        .serverName(guild.getName())
                        .build();
                availableGuilds.add(serverChoice);
            }
            alreadyConsideredServers.add(server.getId());
        }

        List<AServer> restOfKnownServers = serverManagementService.getAllServers()
                .stream()
                .filter(server -> alreadyConsideredServers.contains(server.getId()))
                .toList();
        for (AServer server : restOfKnownServers) {
            boolean possibleForModmail = false;
            Long actualServerId = 0L;
            Long potentialMainServer = configService.getLongValue(ModMailFeatureConfig.MOD_MAIL_APPEAL_SERVER, server.getId()); // what _other_ server is the appeal server
            if(potentialMainServer != 0 && !alreadyConsideredServers.contains(potentialMainServer)) {
                if(featureModeService.featureModeActive(ModMailFeatureDefinition.MOD_MAIL, potentialMainServer, ModMailMode.MOD_MAIL_APPEALS)) {
                    Long configuredAppealServerId = configService.getLongValue(ModMailFeatureConfig.MOD_MAIL_APPEAL_SERVER, potentialMainServer);
                    if(configuredAppealServerId != 0 && configuredAppealServerId.equals(server.getId())) { // if the other server has set the current server as the appeal config
                        Guild otherGuild = guildService.getGuildById(potentialMainServer);
                        if(otherGuild != null) { // check if we are part of that server
                            possibleForModmail = true;
                            actualServerId = potentialMainServer;
                            log.info("Server {} was available, because it is using server {} as a mod mail appeal server.", server.getId(), otherGuild.getIdLong());
                        }
                    }
                } else {
                    log.info("Server {} has set the appeal server {}, but that server does not have mod mail appeals enabled.", server.getId(), potentialMainServer);
                }
            }
            if(possibleForModmail) {
                Guild guild = guildService.getGuildById(actualServerId);
                ServerChoice serverChoice = ServerChoice
                        .builder()
                        .serverId(guild.getIdLong())
                        .serverName(guild.getName())
                        .appealModmail(true)
                        .build();
                availableGuilds.add(serverChoice);
            }
        }
        log.info("There were {} available servers found.", availableGuilds.size());
        // if more than 1 server is available, show a choice dialog
        ArrayList<UndoActionInstance> undoActions = new ArrayList<>();
        if(availableGuilds.size() > 1) {
            Map<String, ServerChoice> choices = new HashMap<>();
            ServerChoices serverChoices = ServerChoices
                    .builder()
                    .commonGuilds(choices)
                    .userId(initialMessage.getAuthor().getIdLong())
                    .messageId(initialMessage.getIdLong())
                    .build();
            availableGuilds.forEach(serverChoice -> choices.put(componentService.generateComponentId(), serverChoice));
            ModMailServerChooserModel modMailServerChooserModel = ModMailServerChooserModel
                    .builder()
                    .choices(serverChoices)
                    .build();
            MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_modal_server_choice", modMailServerChooserModel);
            FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, initialMessage.getChannel()))
                    .thenAccept(unused -> self.persistInitialCallbacks(serverChoices))
                    .exceptionally(throwable -> {
                        log.error("Failed to setup prompt message correctly", throwable);
                        undoActionService.performActions(undoActions);
                        return null;
                    });
            log.debug("Displaying server choice message for user {} in channel {}.", user.getId(), initialMessage.getChannel().getId());
        } else if(availableGuilds.size() == 1) {
            // if exactly one server is available, open the thread directly
            ServerChoice onlyChoice = availableGuilds.get(0);
            Long chosenServerId = onlyChoice.getServerId();
            Guild guild = guildService.getGuildById(chosenServerId);
            boolean appeal = onlyChoice.getAppealModmail();
            log.info("Only one server available to modmail. Directly opening modmail thread for user {} in server {}.", initialMessage.getAuthor().getId(), chosenServerId);
            createModMailThreadForUser(initialMessage.getAuthor(), guild , initialMessage, true, undoActions, appeal)
                .thenAccept(messageChannel -> {
                    log.info("Setup modmail thread for user {} in guild {}.", initialMessage.getAuthor().getIdLong(), guild.getIdLong());
                }).exceptionally(throwable -> {
                    log.error("Failed to setup modmail channel in guild {} for user {}.", guild.getIdLong(), initialMessage.getAuthor().getIdLong(), throwable);
                    return null;
                });
        } else {
            log.info("No server available to open a modmail thread in.");
            // in case there is no server available, send an error message
            channelService.sendEmbedTemplateInMessageChannel("modmail_no_server_available", new Object(), initialMessage.getChannel());
        }
    }

    @Transactional
    public void persistInitialCallbacks(ServerChoices choices) {
        ServiceChoicesPayload payload = ServiceChoicesPayload.fromServerChoices(choices);
        choices.getCommonGuilds().keySet().forEach(componentId ->
                componentPayloadService.createButtonPayload(componentId, payload, MODMAIL_INITIAL_ORIGIN, null));
    }


    /**
     * Method used to send the header of a newly created mod mail thread. This message contains information about
     * the user which the thread is about
     * @param channel The {@link GuildMessageChannel} in which the mod mail thread is present in
     * @param user The {@link User} which the {@link ModMailThread} is about
     */
    private CompletableFuture<Void> sendModMailHeader(GuildMessageChannel channel, User user) {
        log.debug("Sending modmail thread header for tread in channel {} on server {}.", channel.getIdLong(), channel.getGuild().getId());
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(channel.getGuild().getIdLong(), user.getIdLong());
        ModMailThread latestThread = modMailThreadManagementService.getLatestModMailThread(aUserInAServer);
        List<ModMailThread> oldThreads = modMailThreadManagementService.getModMailThreadForUser(aUserInAServer);
        ModMailThreaderHeader header = ModMailThreaderHeader
                .builder()
                .userDisplay(UserDisplay.fromUser(user))
                .latestModMailThread(latestThread)
                .pastModMailThreadCount((long)oldThreads.size())
                .build();
        List<CompletableFuture<Message>> messages = channelService.sendEmbedTemplateInMessageChannel("modmail_thread_header", header, channel);
        return CompletableFuture.allOf(messages.toArray(new CompletableFuture[0]));
    }

    @Override
    public CompletableFuture<Message> relayMessageToModMailThread(ModMailThread modMailThread, Message messageFromUser, List<UndoActionInstance> undoActions) {
        Long serverId = modMailThread.getServer().getId();
        Long channelId = modMailThread.getChannel().getId();
        Long modmailThreadId = modMailThread.getId();
        metricService.incrementCounter(MDOMAIL_THREAD_MESSAGE_RECEIVED);
        log.debug("Relaying message {} to modmail thread {} for user {} to server {}.", messageFromUser.getId(), modMailThread.getId(), messageFromUser.getAuthor().getIdLong(), modMailThread.getServer().getId());
        Optional<GuildMessageChannel> textChannelFromServer = channelService.getMessageChannelFromServerOptional(serverId, channelId);
        if(textChannelFromServer.isPresent()) {
            GuildMessageChannel guildMessageChannel = textChannelFromServer.get();
            return self.sendUserReply(guildMessageChannel, modmailThreadId, messageFromUser, true);
        } else {
            log.warn("Closing mod mail thread {}, because it seems the channel {} in server {} got deleted.", modmailThreadId, channelId, serverId);
            // in this case there was no text channel on the server associated with the mod mail thread
            // close the existing one, so the user can start a new one
            self.closeModMailThreadInDb(modmailThreadId);
            String textToSend = templateService.renderTemplate("modmail_failed_to_forward_message", new Object(), serverId);
            return channelService.sendTextToChannel(textToSend, messageFromUser.getChannel());
        }
    }

    @Override
    public CompletableFuture<Void> sendMessageToUser(AUserInAServer aUserInAServer, MessageToSend messageToSendToUser, User user) {
        AServer aServer = aUserInAServer.getServerReference();
        Long serverId = aServer.getId();
        Long userId = user.getIdLong();
        Guild guild = guildService.getGuildById(serverId);
        if(modMailThreadManagementService.hasOpenModMailThreadForUser(aUserInAServer)) {
            return sendMessageToExistingModmailThread(aUserInAServer, messageToSendToUser, user);
        } else {
            List<UndoActionInstance> undoActionInstances = new ArrayList<>();
            ModmailThreadCreatedSendMessageModel createdSendMessageModel = ModmailThreadCreatedSendMessageModel
                .builder()
                .messageToSend(messageToSendToUser)
                .userId(userId)
                .serverId(serverId)
                .build();
            return createModMailThreadForUser(user, guild, null,  false, undoActionInstances, false, createdSendMessageModel).thenAccept(messageChannel -> {
            }).exceptionally(throwable -> {
              log.error("Failed to setup modmail thread correctly", throwable);
              undoActionService.performActions(undoActionInstances);
              throw new AbstractoRunTimeException(throwable);
            });
        }
    }

    private CompletableFuture<Void> sendMessageToExistingModmailThread(AUserInAServer aUserInAServer, MessageToSend messageToSendToUser, User user) {
        ModMailThread modmailThread = modMailThreadManagementService.getOpenModMailThreadForUser(aUserInAServer);
        CompletableFuture<Message> future = messageService.sendMessageToSendToUser(user, messageToSendToUser);
        CompletableFuture<Message> sameThreadMessageFuture = channelService.sendMessageEmbedToSendToAChannel(messageToSendToUser, modmailThread.getChannel()).get(0);
        Long modmailThreadId = modmailThread.getId();
        Guild guild = guildService.getGuildById(aUserInAServer.getServerReference().getId());
        return CompletableFuture.allOf(future, sameThreadMessageFuture).thenAccept(avoid ->
            self.saveSendMessagesAndUpdateState(modmailThreadId, false, future.join(), guild.getSelfMember(), SnowflakeUtils.createSnowFlake(), sameThreadMessageFuture.join())
        );
    }

    @TransactionalEventListener
    public void sendMessageAfterModmailThreadCreated(ModmailThreadCreatedSendMessageModel model) {
        log.info("Sending message to {} in server {} after modmail thread was created.", model.getUserId(), model.getServerId());
        userService.retrieveUserForId(model.getUserId())
            .thenCompose(user -> self.loadUserAndSendtoModmail(model, user))
            .exceptionally(throwable -> {
                log.warn("Failed to load user to send modmail thread to.");
                return null;
            });
    }

    @Transactional
    public CompletableFuture<Void> loadUserAndSendtoModmail(ModmailThreadCreatedSendMessageModel model, User user) {
        AUserInAServer aUserInAServer = userInServerManagementService.onlyLoadUser(model.getServerId(), model.getUserId());
        return sendMessageToExistingModmailThread(aUserInAServer, model.getMessageToSend(), user);
    }

    /**
     * This message takes a received {@link Message} from a user, renders it to a new message to send and sends it to
     * the appropriate {@link ModMailThread} channel, the returned promise only returns if the message was dealt with on the user
     * side.
     * @param messageChannel The {@link GuildMessageChannel} in which the {@link ModMailThread} is being handled
     * @param modMailThreadId The id of the modmail thread to which the received {@link Message} is a reply to, can be null, if it is null, its the initial message
     * @param messageFromUser The received message from the user
     * @param modMailThreadExists  Whether the modmail thread already exists and is persisted.
     * @return A {@link CompletableFuture} which resolves when the postprocessing of the message is completed (adding read notification, and storing messageIDs)
     */
    public CompletableFuture<Message> sendUserReply(GuildMessageChannel messageChannel, Long modMailThreadId, Message messageFromUser, boolean modMailThreadExists) {
        List<CompletableFuture<Member>> subscriberMemberFutures = new ArrayList<>();
        if(modMailThreadExists) {
            ModMailThread modMailThread = modMailThreadManagementService.getById(modMailThreadId);
            List<ModMailThreadSubscriber> subscriberList = modMailSubscriberManagementService.getSubscribersForThread(modMailThread);
            subscriberList.forEach(modMailThreadSubscriber ->
                subscriberMemberFutures.add(memberService.getMemberInServerAsync(modMailThreadSubscriber.getSubscriber()))
            );
            if(subscriberList.isEmpty()) {
                subscriberMemberFutures.add(CompletableFuture.completedFuture(null));
            }
            log.info("Mentioning {} subscribers for modmail thread {}.", subscriberList.size(), modMailThreadId);
        } else {
            subscriberMemberFutures.add(CompletableFuture.completedFuture(null));
            log.info("Initial setup of modmail - not mentioning subscribers.");
        }
        CompletableFuture<Message> messageFuture = new CompletableFuture<>();
        FutureUtils.toSingleFutureGeneric(subscriberMemberFutures).whenComplete((unused, throwable) -> {
            if(throwable != null) {
                log.warn("Failed to load subscriber users. Still relaying message.", throwable);
            }
            List<Member> subscribers = subscriberMemberFutures
                    .stream()
                    .filter(memberCompletableFuture -> !memberCompletableFuture.isCompletedExceptionally())
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            List<String> imageUrls = messageFromUser
                    .getAttachments()
                    .stream()
                    .filter(Message.Attachment::isImage)
                    .map(Message.Attachment::getProxyUrl)
                    .collect(Collectors.toList());
            Map<String, String> otherAttachments = messageFromUser
                    .getAttachments()
                    .stream()
                    .filter(attachment -> !attachment.isImage())
                    .collect(Collectors.toMap(Message.Attachment::getFileName, Message.Attachment::getUrl));
            ModMailUserReplyModel modMailUserReplyModel = ModMailUserReplyModel
                    .builder()
                    .postedMessage(messageFromUser)
                    .userDisplay(UserDisplay.fromUser(messageFromUser.getAuthor()))
                    .attachedImageUrls(imageUrls)
                    .remainingAttachments(otherAttachments)
                    .subscribers(subscribers)
                    .build();
            MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_user_message", modMailUserReplyModel, messageChannel.getGuild().getIdLong());
            List<CompletableFuture<Message>> completableFutures = channelService.sendMessageToSendToChannel(messageToSend, messageChannel);
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
                    .thenCompose(aVoid -> {
                        log.debug("Adding read reaction to initial message for mod mail thread in channel {}.", messageChannel.getGuild().getId());
                        return reactionService.addReactionToMessageAsync("readReaction", messageChannel.getGuild().getIdLong(), messageFromUser);
                    })
                    .thenApply(aVoid -> {
                        Message createdMessage = completableFutures.get(0).join();
                        if(modMailThreadExists) {
                            self.postProcessSendMessages(messageChannel, createdMessage, messageFromUser);
                        }
                        return messageFuture.complete(createdMessage);
                    }).exceptionally(throwable1 -> {
                        log.error("Failed to forward message to thread.", throwable1);
                        messageFuture.completeExceptionally(throwable1);
                        return null;
                    });
        }).exceptionally(throwable -> {
            messageFuture.completeExceptionally(throwable);
            return null;
        });
        return messageFuture;

    }

    /**
     * This message handles the postprocessing of the messages received by the user. This includes: saving the messageIDs
     * in the database, updating the state of the {@link ModMailThread} and adding the read reaction to the user message
     * @param textChannel The channel in which the message
     * @param messageInModMailThread The actual {@link Message} instance which was sent to the mod mail thread
     * @param messageFromUser The {@link Message} object which was sent from the user
     */
    @Transactional
    public void postProcessSendMessages(GuildMessageChannel textChannel, Message messageInModMailThread, Message messageFromUser) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getByChannelIdOptional(textChannel.getIdLong());
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            log.debug("Adding created message {} based on messeage {} sent from user to modmail thread {} and setting status to {}.", messageInModMailThread.getId(), messageFromUser.getId(), modMailThread.getId(), ModMailThreadState.USER_REPLIED);
            modMailMessageManagementService.addMessageToThread(modMailThread, null, messageInModMailThread, messageFromUser.getIdLong(), modMailThread.getUser(), false, false);
            // update the state of the thread
            modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.USER_REPLIED);
        } else {
            throw new ModMailThreadChannelNotFound();
        }
    }

    @Override
    @Transactional
    public CompletableFuture<Void> loadExecutingMemberAndRelay(Long modmailThreadId, String text, Message replyCommandMessage, boolean anonymous, User user, Guild guild) {
        log.info("Relaying message {} to user {} in modmail thread {} on server {}.", replyCommandMessage.getId(), user.getId(), modmailThreadId, guild.getId());
        return memberService.getMemberInServerAsync(replyCommandMessage.getGuild().getIdLong(), replyCommandMessage.getAuthor().getIdLong())
                .thenCompose(executingMember -> self.relayMessageToDm(modmailThreadId, text, replyCommandMessage, anonymous, user, executingMember));
    }

    @Transactional
    public CompletableFuture<Void> relayMessageToDm(Long modmailThreadId, String text, Message replyCommandMessage, boolean anonymous, User user, Member executingMember) {
        metricService.incrementCounter(MDOMAIL_THREAD_MESSAGE_SENT);
        ModMailThread modMailThread = modMailThreadManagementService.getById(modmailThreadId);
        List<String> imageUrls = replyCommandMessage
                .getAttachments()
                .stream()
                .filter(Message.Attachment::isImage)
                .map(Message.Attachment::getProxyUrl)
                .collect(Collectors.toList());
        Map<String, String> otherAttachments = replyCommandMessage
                .getAttachments()
                .stream()
                .filter(attachment -> !attachment.isImage())
                .collect(Collectors.toMap(Message.Attachment::getFileName, Message.Attachment::getUrl));
        ModMailModeratorReplyModel.ModMailModeratorReplyModelBuilder modMailModeratorReplyModelBuilder = ModMailModeratorReplyModel
                .builder()
                .text(text)
                .modMailThread(modMailThread)
                .postedMessage(replyCommandMessage)
                .remainingAttachments(otherAttachments)
                .attachedImageUrls(imageUrls)
                .anonymous(anonymous)
                .userDisplay(UserDisplay.fromUser(user));
        if(anonymous) {
            log.debug("Message is sent anonymous.");
            modMailModeratorReplyModelBuilder.moderator(memberService.getBotInGuild(modMailThread.getServer()));
        } else {
            modMailModeratorReplyModelBuilder.moderator(executingMember);
        }
        ModMailModeratorReplyModel modMailUserReplyModel = modMailModeratorReplyModelBuilder.build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(MODMAIL_STAFF_MESSAGE_TEMPLATE_KEY, modMailUserReplyModel, modMailThread.getServer().getId());
        CompletableFuture<Message> future = messageService.sendMessageToSendToUser(user, messageToSend);
        CompletableFuture<Message> sameThreadMessageFuture;
        if(featureModeService.featureModeActive(ModMailFeatureDefinition.MOD_MAIL, modMailThread.getServer(), ModMailMode.SEPARATE_MESSAGE)) {
            sameThreadMessageFuture = channelService.sendMessageEmbedToSendToAChannel(messageToSend, modMailThread.getChannel()).get(0);
        } else {
            sameThreadMessageFuture = CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.allOf(future, sameThreadMessageFuture).thenAccept(avoid ->
                self.saveSendMessagesAndUpdateState(modmailThreadId, anonymous, future.join(), replyCommandMessage.getMember(), replyCommandMessage.getIdLong(), sameThreadMessageFuture.join())
        );
    }

    @Override
    public CompletableFuture<Void> closeModMailThreadEvaluateLogging(ModMailThread modMailThread, ClosingContext closingConfig, List<UndoActionInstance> undoActions) {
        boolean loggingMode = featureModeService.featureModeActive(ModMailFeatureDefinition.MOD_MAIL, modMailThread.getServer(), ModMailMode.LOGGING);
        closingConfig.setLog(closingConfig.getLog() && loggingMode);
        return closeModMailThread(modMailThread, closingConfig, undoActions);
    }

    @Override
    public CompletableFuture<Void> closeModMailThread(ModMailThread modMailThread, ClosingContext closingConfig, List<UndoActionInstance> undoActions) {
        metricService.incrementCounter(MODMAIL_THREAD_CLOSED_COUNTER);
        Long modMailThreadId = modMailThread.getId();
        log.info("Starting closing procedure for thread {}", modMailThread.getId());
        List<ModMailMessage> modMailMessages = modMailThread.getMessages();
        Long userId = modMailThread.getUser().getUserReference().getId();
        Long serverId = modMailThread.getServer().getId();
        if (featureModeService.featureModeActive(ModMailFeatureDefinition.MOD_MAIL, serverId, ModMailMode.THREAD_CONTAINER)) {
            ThreadChannel threadChannel = channelService.getThreadChannel(modMailThread.getChannel().getId());
            log.info("Archiving thread {} for modmail thread closing.", modMailThread.getChannel().getId());
            return loadUserAndSendClosingHeader(modMailThread, closingConfig)
                    .thenCompose(unused -> channelService.archiveThreadChannel(threadChannel))
                    .thenCompose(unused -> userService.retrieveUserForId(userId))
                    .thenCompose(user -> self.afterSuccessfulLog(modMailThreadId, closingConfig.getNotifyUser(), user, undoActions));
        } else {
            if(closingConfig.getLog()) {
                if(!modMailMessages.isEmpty()) {
                    return modMailMessageService.loadModMailMessages(modMailMessages)
                            .thenCompose(loadedModmailThreadMessages -> self.logMessagesToModMailLog(closingConfig, modMailThreadId, undoActions, loadedModmailThreadMessages, serverId, userId));
                } else {
                    log.info("Modmail thread {} in server {} has no messages. Only logging header.", modMailThreadId, serverId);
                    return loadUserAndSendClosingHeader(modMailThread, closingConfig)
                            .thenCompose(unused -> userService.retrieveUserForId(modMailThread.getUser().getUserReference().getId()).thenCompose(user ->
                                    self.afterSuccessfulLog(modMailThreadId, closingConfig.getNotifyUser(), user, undoActions)
                            ));
                }
            } else {
                log.debug("Not logging modmail thread {}.", modMailThreadId);
                return userService.retrieveUserForId(modMailThread.getUser().getUserReference().getId()).thenCompose(user ->
                        self.afterSuccessfulLog(modMailThreadId, closingConfig.getNotifyUser(), user, undoActions)
                );
            }
        }
    }

    @Override
    public boolean isModMailThread(AChannel channel) {
        return modMailThreadManagementService.getByChannelOptional(channel).isPresent();
    }

    @Override
    public boolean isModMailThread(Long channelId) {
        AChannel channel = channelManagementService.loadChannel(channelId);
        return isModMailThread(channel);
    }

    /**
     * This method takes the actively loaded futures, calls the method responsible for logging the messages, and calls the method
     * after the logging has been done.
     * @param modMailThreadId The ID of the {@link ModMailThread} which is being closed
     * @param undoActions The list of {@link UndoActionInstance} to execute in case of exceptions
     * @param messages The list of loaded {@link Message} to log
     * @param serverId The ID of the {@link Guild} the {@link ModMailThread} is in
     * @param userId The ID of the user the {@link ModMailThread} is about
     * @return A {@link CompletableFuture future} which completes when the messages have been logged
     */
    @Transactional
    public CompletableFuture<Void> logMessagesToModMailLog(ClosingContext closingContext, Long modMailThreadId, List<UndoActionInstance> undoActions,
                                                           ModmailLoggingThreadMessages messages, Long serverId, Long userId) {
        log.debug("Logging {} modmail messages for modmail thread {}.", messages.getMessages().size(), modMailThreadId);
        try {
            return self.logModMailThread(modMailThreadId, messages, closingContext, undoActions, serverId)
                .thenCompose(list -> list.getMainFuture().thenCompose(unused -> {
                    list.getFutures().forEach(messageCompletableFuture -> {
                        Message message = messageCompletableFuture.join();
                        if(message != null) {
                            undoActions.add(UndoActionInstance.getMessageDeleteAction(message.getGuild().getIdLong(), message.getChannel().getIdLong(), message.getIdLong()));
                        }
                    });
                    return userService.retrieveUserForId(userId).thenCompose(user ->
                            self.afterSuccessfulLog(modMailThreadId, closingContext.getNotifyUser(), user, undoActions)
                    ).exceptionally(throwable -> {
                        log.warn("Failed to retrieve member for closing the modmail thread. Closing without member information.", throwable);
                        self.afterSuccessfulLog(modMailThreadId, false, null, undoActions);
                        return null;
                    });
                }));
        } catch (Exception e) {
            log.error("Failed to log mod mail messages", e);
            throw new AbstractoRunTimeException(e);
        }
    }

    /**
     * This message is executed after the thread has been logged and notifies the user about the closed {@link ModMailThread}
     * which a configurable closing text. This method then calls the method to delete the channel.
     * @param modMailThreadId The ID of the {@link ModMailThread} which is being closed.
     * @param notifyUser Whether the user should be notified
     * @param undoActions The list of {@link UndoActionInstance} to execute in case of exceptions
     * @param modMailThreaduser The {@link User member} for which the {@link ModMailThread thread} was for
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     * @return A {@link CompletableFuture future} which completes after the messages have been logged
     */
    @Transactional
    public CompletableFuture<Void> afterSuccessfulLog(Long modMailThreadId, Boolean notifyUser, User modMailThreaduser, List<UndoActionInstance> undoActions) {
        log.debug("Mod mail logging for thread {} has completed. Starting post logging activities.", modMailThreadId);
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getByIdOptional(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            if(notifyUser) {
                log.info("Notifying user about the closed modmail thread {}.", modMailThreadId);
                ModMailThread modMailThread = modMailThreadOpt.get();
                HashMap<String, String> closingMessage = new HashMap<>();
                Long serverId = modMailThread.getServer().getId();
                String defaultValue = templateService.renderSimpleTemplate("modmail_closing_user_message_description", serverId);
                closingMessage.put("closingMessage", configService.getStringValue(MOD_MAIL_CLOSING_TEXT_SYSTEM_CONFIG_KEY, serverId, defaultValue));
                return messageService.sendEmbedToUser(modMailThreaduser, "modmail_closing_user_message", closingMessage).thenCompose(message ->
                    self.deleteChannelAndClose(modMailThreadId, undoActions)
                ).exceptionally(throwable -> {
                    self.deleteChannelAndClose(modMailThreadId, undoActions);
                    return null;
                });
            } else {
                log.info("NOT Notifying user about the closed modmail thread {}.", modMailThreadId);
                return deleteChannelAndClose(modMailThreadId, undoActions);
            }
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }
    }

    /**
     * Deletes the actual {@link MessageChannel} in which the {@link ModMailThread} happened. This method then calls the
     * method to update the stats in the database
     * @param modMailThreadId The ID of the {@link ModMailThread} to delete the {@link MessageChannel} from
     * @param undoActions The list of {@link UndoActionInstance} to execute in case of exceptions
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     * @return A {@link CompletableFuture future} which completes after the {@link TextChannel channel} in which the thread was has been deleted
     */
    @Transactional
    public CompletableFuture<Void> deleteChannelAndClose(Long modMailThreadId, List<UndoActionInstance> undoActions) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getByIdOptional(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            String failureMessage = "Failed to delete text channel containing mod mail thread {}";
            try {
                if (featureModeService.featureModeActive(ModMailFeatureDefinition.MOD_MAIL, modMailThread.getServer().getId(), ModMailMode.THREAD_CONTAINER)) {
                    undoActions.clear();
                    self.closeModMailThreadInDb(modMailThreadId);
                    return CompletableFuture.completedFuture(null);
                } else {
                    log.debug("Deleting channel {} which contained the modmail thread {}.", modMailThread.getChannel().getId(), modMailThreadId);
                    return channelService.deleteTextChannel(modMailThread.getChannel()).thenAccept(avoid -> {
                        undoActions.clear();
                        self.closeModMailThreadInDb(modMailThreadId);
                    });
                }
            } catch (InsufficientPermissionException ex) {
                log.error(failureMessage, modMailThreadId, ex);
                String message = "Failed To delete mod mail thread channel because no permissions.";
                throw new AbstractoTemplatedException(message, "modmail_exception_cannot_delete_channel", ex);
            } catch (Exception ex) {
                log.error(failureMessage, modMailThreadId, ex);
                throw new AbstractoRunTimeException(ex);
            }
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }
    }

    /**
     * Takes the list of {@link CompletableFuture} which are returned from retrieving the {@link Message} to log,
     * and creates the models necessary to render the log entries. This message also sends the closing header in the
     * log concerning general information about the closed {@link ModMailThread}
     * @param modMailThreadId The ID of the {@link ModMailThread} to log the messages of
     * @param messages The list of {@link CompletableFuture} which contain the {@link Message} which could be loaded
     * @param undoActions A list of {@link dev.sheldan.abstracto.core.models.UndoAction actions} to be undone in case the operation fails. This list will be filled in the method.
     * @param serverId The ID of the {@link Guild server} the modmail thread is in
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     * @return An instance of {@link CompletableFutureList}, which contains a main {@link CompletableFuture} which is resolved,
     * when all of the smaller {@link CompletableFuture} in it are resolved. We need this construct, because we need to access
     * the result values of the individual futures after they are done.
     */
    @Transactional
    public CompletableFuture<CompletableFutureList<Message>> logModMailThread(Long modMailThreadId, ModmailLoggingThreadMessages messages,
                                                                              ClosingContext context, List<UndoActionInstance> undoActions, Long serverId) {
        log.info("Logging mod mail thread {} with {} messages.", modMailThreadId, messages.getMessages().size());
        if(messages.getMessages().isEmpty()) {
            log.info("Modmail thread {} is empty. No messages to log.", modMailThreadId);
            return CompletableFuture.completedFuture(new CompletableFutureList<>(new ArrayList<>()));
        }
        GuildMessageChannel channel = channelService.getMessageChannelFromServer(serverId, modMailThreadId);
        ClosingProgressModel progressModel = ClosingProgressModel
                .builder()
                .loggedMessages(0)
                .totalMessages(messages.getMessages().size())
                .build();
        List<CompletableFuture<Message>> updateMessageFutures = channelService.sendEmbedTemplateInMessageChannel(MODMAIL_CLOSE_PROGRESS_TEMPLATE_KEY, progressModel, channel);
        return FutureUtils.toSingleFutureGeneric(updateMessageFutures)
                .thenCompose(updateMessage -> self.logMessages(modMailThreadId, messages, context, updateMessageFutures.get(0).join()));
    }

    @Transactional
    public CompletableFuture<CompletableFutureList<Message>> logMessages(Long modMailThreadId, ModmailLoggingThreadMessages messages, ClosingContext context, Message updateMessage) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getByIdOptional(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            List<ModMailLoggedMessageModel> loggedMessages = new ArrayList<>();
            Map<Long, User> authors = messages
                    .getAuthors()
                    .stream().collect(Collectors.toMap(ISnowflake::getIdLong, Function.identity()));
            messages.getMessages().forEach(message -> {
                log.info("Logging message {} in modmail thread {}.", message.getId(), modMailThreadId);
                ModMailMessage modmailMessage = modMailThread.getMessages()
                        .stream()
                        .filter(modMailMessage -> {
                            if(modMailMessage.getDmChannel()) {
                                return modMailMessage.getCreatedMessageInDM().equals(message.getIdLong());
                            } else {
                                return modMailMessage.getCreatedMessageInChannel().equals(message.getIdLong());
                            }
                        })
                        .findFirst()
                        .orElseThrow(() -> new AbstractoRunTimeException("Could not find desired message in list of messages in thread. This should not happen, as we just retrieved them from the same place."));
                User author = authors.getOrDefault(modmailMessage.getAuthor().getUserReference().getId(), message.getJDA().getSelfUser());
                ModMailLoggedMessageModel modMailLoggedMessageModel =
                        ModMailLoggedMessageModel
                                .builder()
                                .message(message)
                                .author(author)
                                .modMailMessage(modmailMessage)
                                .build();
                loggedMessages.add(modMailLoggedMessageModel);
            });
            List<CompletableFuture<Message>> completableFutures = new ArrayList<>();
            log.debug("Sending close header and individual mod mail messages to mod mail log target for thread {}.", modMailThreadId);
            CompletableFuture<Message> headerFuture = loadUserAndSendClosingHeader(modMailThread, context);
            completableFutures.add(headerFuture);
            return headerFuture.thenApply(message -> {
                completableFutures.addAll(self.sendMessagesToPostTarget(modMailThreadId, loggedMessages, updateMessage));
                return new CompletableFutureList<>(completableFutures);
            });
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }
    }

    private CompletableFuture<Message> loadUserAndSendClosingHeader(ModMailThread modMailThread, ClosingContext closingContext) {
        ModMailClosingHeaderModel headerModel = ModMailClosingHeaderModel
                .builder()
                .closingMember(closingContext.getClosingMember())
                .note(closingContext.getNote())
                .silently(!closingContext.getNotifyUser())
                .messageCount(modMailThread.getMessages().size())
                .startDate(modMailThread.getCreated())
                .serverId(modMailThread.getServer().getId())
                .userId(modMailThread.getUser().getUserReference().getId())
                .build();
        Long modmailThreadId = modMailThread.getId();
        return userService.retrieveUserForId(modMailThread.getUser().getUserReference().getId()).thenApply(user -> {
            headerModel.setUser(UserDisplay.fromUser(user));
            return self.sendClosingHeader(headerModel, modmailThreadId).get(0);
        }).thenCompose(Function.identity());
    }

    @Transactional
    public List<CompletableFuture<Message>> sendClosingHeader(ModMailClosingHeaderModel model, Long modmailThreadId) {
        MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_close_header", model, model.getServerId());
        if (featureModeService.featureModeActive(ModMailFeatureDefinition.MOD_MAIL, model.getServerId(), ModMailMode.THREAD_CONTAINER)) {
            ModMailThread modMailThread = modMailThreadManagementService.getById(modmailThreadId);
            return channelService.sendMessageEmbedToSendToAChannel(messageToSend, modMailThread.getChannel());
        } else {
            return postTargetService.sendEmbedInPostTarget(messageToSend, ModMailPostTargets.MOD_MAIL_LOG, model.getServerId());
        }
    }

    /**
     * Sets the {@link ModMailThread} in the database to CLOSED.
     * @param modMailThreadId The ID of the {@link ModMailThread} to update the state of
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     */
    @Transactional
    public void closeModMailThreadInDb(Long modMailThreadId) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getByIdOptional(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            log.info("Setting thread {} to closed in db.", modMailThread.getId());
            modMailThread.setClosed(Instant.now());
            modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.CLOSED);
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }
    }

    /**
     * Renders the retrieved {@link Message} which are in {@link ModMailLoggedMessageModel} into {@link MessageToSend} and
     * sends this to the appropriate logging {@link PostTarget}
     * @param modMailThreadId The ID of {@link ModMailThread} to which the loaded messages belong to
     * @param loadedMessages The list of {@link ModMailLoggedMessageModel} which can be rendered
     * @return A list of {@link CompletableFuture} which represent each of the messages being sent to the {@link PostTarget}
     */
    public List<CompletableFuture<Message>> sendMessagesToPostTarget(Long modMailThreadId, List<ModMailLoggedMessageModel> loadedMessages, Message updateMessage) {
        List<CompletableFuture<Message>> messageFutures = new ArrayList<>();
        ClosingProgressModel progressModel = ClosingProgressModel
                .builder()
                .loggedMessages(0)
                .totalMessages(loadedMessages.size())
                .build();
        loadedMessages = loadedMessages.stream().sorted(Comparator.comparing(o -> o.getMessage().getTimeCreated())).collect(Collectors.toList());
        for (int i = 0; i < loadedMessages.size(); i++) {
            ModMailLoggedMessageModel message = loadedMessages.get(i);
            log.debug("Sending message {} of modmail thread {} to modmail log post target.", modMailThreadId, message.getMessage().getId());
            MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_close_logged_message", message, updateMessage.getGuild().getIdLong());
            List<CompletableFuture<Message>> logFuture = postTargetService.sendEmbedInPostTarget(messageToSend, ModMailPostTargets.MOD_MAIL_LOG, updateMessage.getGuild().getIdLong());
            if(i != 0 && (i % 10) == 0) {
                progressModel.setLoggedMessages(i);
                messageService.editMessageWithNewTemplate(updateMessage, MODMAIL_CLOSE_PROGRESS_TEMPLATE_KEY, progressModel);
            }
            messageFutures.addAll(logFuture);
        }
        return messageFutures;
    }

    /**
     * Evaluates the promises which were created when sending the messages to the private channel and stores the message IDs
     * and updates the state of the {@link ModMailThread}.
     * @param modMailThreadId The ID of the {@link ModMailThread} for which the messages were sent for
     * @param anonymous Whether or not the messages were send anonymous
     * @param createdMessageInDM The {@link Message message} which was sent to the private channel with the {@link User user}
     * @param modMailThreadMessage The {@link Message message} which was sent in the channel representing the {@link ModMailThread thread}. Might be null.
     * @param author The {@link Member} which caused the message to be sent
     * @param messageId The ID of the message that was sent
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     */
    @Transactional
    public void saveSendMessagesAndUpdateState(Long modMailThreadId, Boolean anonymous, Message createdMessageInDM, Member author, Long messageId, Message modMailThreadMessage) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getByIdOptional(modMailThreadId);
        AUserInAServer moderator = userInServerManagementService.loadOrCreateUser(author);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            log.debug("Adding (anonymous: {}) message {} of moderator to modmail thread {} and setting state to {}.", anonymous, createdMessageInDM.getId(), modMailThreadId, ModMailThreadState.MOD_REPLIED);
            modMailMessageManagementService.addMessageToThread(modMailThread, createdMessageInDM, modMailThreadMessage, messageId, moderator, anonymous, true);
            modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.MOD_REPLIED);
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }
    }

    @Transactional
    public CompletableFuture<Void> banUserFromAppealServer(Long mainServerId, Long userId, String reason) {
        Long configuredAppealServerId = configService.getLongValue(ModMailFeatureConfig.MOD_MAIL_APPEAL_SERVER, mainServerId);
        Guild appealGuild = guildService.getGuildById(configuredAppealServerId);
        return banService.banUser(appealGuild, ServerUser.fromId(configuredAppealServerId,  userId), Duration.ZERO, reason);
    }

    @Override
    public CompletableFuture<Void> rejectAppeal(ModMailThread modMailThread, String reason, Member memberPerforming) {
        ClosingContext context = ClosingContext
                .builder()
                .closingMember(memberPerforming)
                .notifyUser(true)
                .log(true)
                .note(reason)
                .build();
        Long mainServerId = modMailThread.getServer().getId();
        Long userToBanId = modMailThread.getUser().getUserReference().getId();
        return closeModMailThread(modMailThread, context, new ArrayList<>())
                .thenCompose((nul) -> self.banUserFromAppealServer(mainServerId, userToBanId , reason));
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(MODMAIL_THREAD_CREATED_COUNTER, "Mod mail threads created");
        metricService.registerCounter(MODMAIL_THREAD_CLOSED_COUNTER, "Mod mail threads closed");
        metricService.registerCounter(MDOMAIL_THREAD_MESSAGE_RECEIVED, "Mod mail messages received");
        metricService.registerCounter(MDOMAIL_THREAD_MESSAGE_SENT, "Mod mail messages sent");
    }
}
