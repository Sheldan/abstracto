package dev.sheldan.abstracto.modmail.service;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.FullGuild;
import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.UndoActionInstance;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureConfig;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailMode;
import dev.sheldan.abstracto.modmail.config.ModMailPostTargets;
import dev.sheldan.abstracto.modmail.exception.ModMailCategoryIdException;
import dev.sheldan.abstracto.modmail.exception.ModMailThreadChannelNotFound;
import dev.sheldan.abstracto.modmail.exception.ModMailThreadNotFoundException;
import dev.sheldan.abstracto.modmail.model.ClosingContext;
import dev.sheldan.abstracto.modmail.model.database.*;
import dev.sheldan.abstracto.modmail.model.dto.ServerChoice;
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
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ModMailThreadServiceBean implements ModMailThreadService {

    /**
     * The config key to use for the closing text
     */
    public static final String MODMAIL_CLOSING_MESSAGE_TEXT = "modMailClosingText";
    /**
     * The config key to use for the ID of the category to create {@link MessageChannel} in
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
    private EventWaiter eventWaiter;

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

    /**
     * The emoji used when the user can decide for a server to open a mod mail thread in.
     */
    private static List<String> NUMBER_EMOJI = Arrays.asList("\u0031\u20e3", "\u0032\u20e3", "\u0033\u20e3",
            "\u0034\u20e3", "\u0035\u20e3", "\u0036\u20e3",
            "\u0037\u20e3", "\u0038\u20e3", "\u0039\u20e3",
            "\u0040\u20e3");


    @Override
    public CompletableFuture<Void> createModMailThreadForUser(Member member, Message initialMessage, MessageChannel feedBackChannel, boolean userInitiated, List<UndoActionInstance> undoActions) {
        Long serverId = member.getGuild().getIdLong();
        Long categoryId = configService.getLongValue(MODMAIL_CATEGORY, serverId);
        AServer server = serverManagementService.loadServer(member.getGuild().getIdLong());
        metricService.incrementCounter(MODMAIL_THREAD_CREATED_COUNTER);
        User user = member.getUser();
        log.info("Creating modmail channel for user {} in category {} on server {}.", user.getId(), categoryId, serverId);
        ModMailChannelNameModel model = ModMailChannelNameModel
                .builder()
                .serverId(serverId)
                .userId(member.getIdLong())
                .randomText(RandomStringUtils.randomAlphanumeric(25))
                .uuid(UUID.randomUUID().toString())
                .currentDate(Instant.now())
                .build();
        String channelName = templateService.renderTemplate(TEXT_CHANNEL_NAME_TEMPLATE_KEY, model, serverId);
        CompletableFuture<TextChannel> textChannelFuture = channelService.createTextChannel(channelName, server, categoryId);
        return textChannelFuture.thenCompose(channel -> {
            undoActions.add(UndoActionInstance.getChannelDeleteAction(serverId, channel.getIdLong()));
            return self.performModMailThreadSetup(member, initialMessage, channel, userInitiated, undoActions, feedBackChannel);
        });
    }

    @Transactional
    public CompletableFuture<Void> sendContactNotification(Member member, TextChannel textChannel, MessageChannel feedBackChannel) {
        ContactNotificationModel model = ContactNotificationModel
                .builder()
                .createdChannel(textChannel)
                .targetMember(member)
                .build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInMessageChannelList(MODMAIL_THREAD_CREATED_TEMPLATE_KEY, model, feedBackChannel));
    }

    /**
     * This method is responsible for creating the instance in the database, sending the header in the newly created text channel and forwarding the initial message
     * by the user (if any), after this is complete, this method executes the method to perform the mod mail notification.
     * @param member The {@link Member} for which a {@link ModMailThread} is being created
     * @param initialMessage The {@link Message} which was sent by the user to open a thread, this is null, if the thread was opened via a command
     * @param channel The created {@link TextChannel} in which the mod mail thread is dealt with
     * @param userInitiated Whether or not the thread was initiated by a member
     * @param undoActions The list of actions to undo, in case an exception occurs
     * @return A {@link CompletableFuture future} which completes when the setup is done
     */
    @Transactional
    public CompletableFuture<Void> performModMailThreadSetup(Member member, Message initialMessage, TextChannel channel, boolean userInitiated, List<UndoActionInstance> undoActions, MessageChannel feedBackChannel) {
        log.info("Performing modmail thread setup for channel {} for user {} in server {}. It was initiated by a user: {}.", channel.getIdLong(), member.getId(), channel.getGuild().getId(), userInitiated);
        CompletableFuture<Void> headerFuture = sendModMailHeader(channel, member);
        CompletableFuture<Message> userReplyMessage;
        if(initialMessage != null){
            log.debug("Sending initial message {} of user {} to modmail thread {}.", initialMessage.getId(), member.getId(), channel.getId());
            userReplyMessage = self.sendUserReply(channel, 0L, initialMessage, member, false);
        } else {
            log.debug("No initial message to send.");
            userReplyMessage = CompletableFuture.completedFuture(null);
        }
        CompletableFuture notificationFuture;
        if (userInitiated) {
            notificationFuture = self.sendModMailNotification(member, channel);
        } else {
            notificationFuture = CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.allOf(headerFuture, notificationFuture, userReplyMessage).thenAccept(aVoid -> {
            undoActions.clear();
            self.setupModMailThreadInDB(initialMessage, channel, member, userReplyMessage.join());
        }).thenAccept(unused -> {
            if(!userInitiated) {
                self.sendContactNotification(member, channel, feedBackChannel);
            }
        });
    }

    @Transactional
    public void setupModMailThreadInDB(Message initialMessage, TextChannel channel, Member member, Message sendMessage) {
        log.info("Persisting info about modmail thread {} in database.", channel.getIdLong());
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
        ModMailThread thread = createThreadObject(channel, aUserInAServer);
        if(initialMessage != null) {
            log.debug("Adding initial message {} to modmail thread in channel {}.", initialMessage.getId(), channel.getId());
            modMailMessageManagementService.addMessageToThread(thread, null, sendMessage, initialMessage, aUserInAServer, false, false);
        }
    }

    /**
     * Sends the message containing the pings to notify the staff members to handle the opened {@link ModMailThread}
     * @param member The {@link FullUserInServer} which opened the thread
     * @param channel The created {@link TextChannel} in which the mod mail thread is dealt with
     * @return A {@link CompletableFuture future} which complets when the notification has been sent
     */
    @Transactional
    public CompletableFuture<Void> sendModMailNotification(Member member, TextChannel channel) {
        Long serverId = member.getGuild().getIdLong();
        log.info("Sending modmail notification for new modmail thread about user {} in server {}.", member.getId(), serverId);
        AServer server = serverManagementService.loadServer(serverId);
        List<ModMailRole> rolesToPing = modMailRoleManagementService.getRolesForServer(server);
        log.debug("Pinging {} roles to notify about modmail thread about user {} in server {}.", rolesToPing.size(), member.getId(), serverId);
        ModMailNotificationModel modMailNotificationModel = ModMailNotificationModel
                .builder()
                .member(member)
                .roles(rolesToPing)
                .channel(channel)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_notification_message", modMailNotificationModel, channel.getGuild().getIdLong());
        List<CompletableFuture<Message>> modmailping = postTargetService.sendEmbedInPostTarget(messageToSend, ModMailPostTargets.MOD_MAIL_PING, serverId);
        return CompletableFuture.allOf(modmailping.toArray(new CompletableFuture[0]));
    }

    /**
     * Creates the instance of the {@link ModMailThread} in the database.
     * @param channel The {@link TextChannel} in which the {@link ModMailThread} is being done
     * @param user The {@link AUserInAServer} which the thread is about
     * @return The created instance of {@link ModMailThread}
     */
    public ModMailThread createThreadObject(TextChannel channel, AUserInAServer user) {
        log.info("Creating database objects related to modmail thread in channel {} and about user {} in server {}.", channel.getIdLong(), user.getUserReference().getId(), channel.getGuild().getId());
        AChannel channel2 = channelManagementService.createChannel(channel.getIdLong(), AChannelType.TEXT, user.getServerReference());
        log.info("Creating mod mail thread in channel {} with db channel {}", channel.getIdLong(), channel2.getId());
        return modMailThreadManagementService.createModMailThread(user, channel2);
    }

    @Override
    public void setModMailCategoryTo(Guild guild, Long categoryId) {
        log.info("Trying to set modmail category to {} in guild {}.", categoryId, guild.getId());
        FeatureValidationResult result = FeatureValidationResult.builder().build();
        modMailFeatureValidator.validateModMailCategory(result, guild, categoryId);
        if(result.getValidationResult()) {
            throw new ModMailCategoryIdException(categoryId);
        }
        configService.setLongValue(MODMAIL_CATEGORY, guild.getIdLong(), categoryId);
    }

    @Override
    public void createModMailPrompt(AUser user, Message initialMessage) {
        List<AUserInAServer> knownServers = userInServerManagementService.getUserInAllServers(user.getId());
        // if the user doesnt exist in the servery set, we need to create the user first in all of them, in order to offer it
        if(knownServers.isEmpty()) {
            List<Guild> mutualServers = initialMessage.getJDA().getMutualGuilds(initialMessage.getAuthor());
            mutualServers.forEach(guild -> {
                AServer server = serverManagementService.loadServer(guild);
                knownServers.add(userInServerManagementService.loadOrCreateUser(server, user));
            });
        }
        if(!knownServers.isEmpty()) {
            log.info("There are {} shared servers between user and the bot.", knownServers.size());
            List<ServerChoice> availableGuilds = new ArrayList<>();
            HashMap<String, Long> choices = new HashMap<>();
            for (int i = 0; i < knownServers.size(); i++) {
                AUserInAServer aUserInAServer = knownServers.get(i);
                // only take the servers in which mod mail is actually enabled, would not make much sense to make the
                // other servers available
                if(featureFlagService.isFeatureEnabled(modMailFeatureConfig, aUserInAServer.getServerReference())) {
                    AServer serverReference = aUserInAServer.getServerReference();
                    FullGuild guild = FullGuild
                            .builder()
                            .guild(guildService.getGuildById(serverReference.getId()))
                            .server(serverReference)
                            .build();
                    // TODO support more than this limited amount of servers
                    String reactionEmote = NUMBER_EMOJI.get(i);
                    ServerChoice serverChoice = ServerChoice.builder().guild(guild).reactionEmote(reactionEmote).build();
                    choices.put(reactionEmote, aUserInAServer.getServerReference().getId());
                    availableGuilds.add(serverChoice);
                }
            }
            log.info("There were {} shared servers found which have modmail enabled.", availableGuilds.size());
            // if more than 1 server is available, show a choice dialog
            if(availableGuilds.size() > 1) {
                ModMailServerChooserModel modMailServerChooserModel = ModMailServerChooserModel
                        .builder()
                        .commonGuilds(availableGuilds)
                        .build();
                String text = templateService.renderTemplate("modmail_modal_server_choice", modMailServerChooserModel);
                ButtonMenu menu = new ButtonMenu.Builder()
                        .setChoices(choices.keySet().toArray(new String[0]))
                        .setEventWaiter(eventWaiter)
                        .setDescription(text)
                        .setAction(reactionEmote -> {
                            Long chosenServerId = choices.get(reactionEmote.getEmoji());
                            Long userId = initialMessage.getAuthor().getIdLong();
                            log.debug("Executing action for creationg a modmail thread in server {} for user {}.", chosenServerId, userId);
                            memberService.getMemberInServerAsync(chosenServerId, userId).thenCompose(member -> {
                                        try {
                                            return self.createModMailThreadForUser(member, initialMessage, initialMessage.getChannel(), true, new ArrayList<>());
                                        } catch (Exception exception) {
                                            log.error("Setting up modmail thread for user {} in server {} failed.", userId, chosenServerId, exception);
                                            CompletableFuture<Void> future = new CompletableFuture<>();
                                            future.completeExceptionally(exception);
                                            return future;
                                        }
                                    }).exceptionally(throwable -> {
                                log.error("Failed to load member {} for modmail in server {}.", userId, chosenServerId, throwable);
                                return null;
                            });
                        })
                        .build();
                log.debug("Displaying server choice message for user {} in channel {}.", user.getId(), initialMessage.getChannel().getId());
                menu.display(initialMessage.getChannel());
            } else if(availableGuilds.size() == 1) {
                // if exactly one server is available, open the thread directly
                Long chosenServerId = choices.get(availableGuilds.get(0).getReactionEmote());
                log.info("Only one server available to modmail. Directly opening modmail thread for user {} in server {}.", initialMessage.getAuthor().getId(), chosenServerId);
                memberService.getMemberInServerAsync(chosenServerId, initialMessage.getAuthor().getIdLong()).thenCompose(member -> {
                            try {
                                return self.createModMailThreadForUser(member, initialMessage, initialMessage.getChannel(), true, new ArrayList<>());
                            } catch (Exception exception) {
                                CompletableFuture<Void> future = new CompletableFuture<>();
                                future.completeExceptionally(exception);
                                return future;
                            }
                        }).exceptionally(throwable -> {
                            log.error("Failed to setup thread correctly", throwable);
                            return null;
                        });
            } else {
                log.info("No server available to open a modmail thread in.");
                // in case there is no server available, send an error message
                channelService.sendEmbedTemplateInMessageChannelList("modmail_no_server_available", new Object(), initialMessage.getChannel());
            }
        } else {
            log.warn("User {} which was not known in any of the servers tried to contact the bot.", user.getId());
        }
    }


    /**
     * Method used to send the header of a newly created mod mail thread. This message contains information about
     * the user which the thread is about
     * @param channel The {@link TextChannel} in which the mod mail thread is present in
     * @param member The {@link Member} which the {@link ModMailThread} is about
     */
    private CompletableFuture<Void> sendModMailHeader(TextChannel channel, Member member) {
        log.debug("Sending modmail thread header for tread in channel {} on server {}.", channel.getIdLong(), channel.getGuild().getId());
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
        ModMailThread latestThread = modMailThreadManagementService.getLatestModMailThread(aUserInAServer);
        List<ModMailThread> oldThreads = modMailThreadManagementService.getModMailThreadForUser(aUserInAServer);
        ModMailThreaderHeader header = ModMailThreaderHeader
                .builder()
                .member(member)
                .latestModMailThread(latestThread)
                .pastModMailThreadCount((long)oldThreads.size())
                .build();
        List<CompletableFuture<Message>> messages = channelService.sendEmbedTemplateInTextChannelList("modmail_thread_header", header, channel);
        return CompletableFuture.allOf(messages.toArray(new CompletableFuture[0]));
    }

    @Override
    public CompletableFuture<Message> relayMessageToModMailThread(ModMailThread modMailThread, Message messageFromUser, List<UndoActionInstance> undoActions) {
        Long serverId = modMailThread.getServer().getId();
        Long channelId = modMailThread.getChannel().getId();
        Long modmailThreadId = modMailThread.getId();
        metricService.incrementCounter(MDOMAIL_THREAD_MESSAGE_RECEIVED);
        log.debug("Relaying message {} to modmail thread {} for user {} to server {}.", messageFromUser.getId(), modMailThread.getId(), messageFromUser.getAuthor().getIdLong(), modMailThread.getServer().getId());
        return memberService.getMemberInServerAsync(modMailThread.getServer().getId(), messageFromUser.getAuthor().getIdLong()).thenCompose(member ->
            self.relayMessage(messageFromUser, serverId, channelId, modmailThreadId, member)
        );

    }

    @Transactional
    public CompletableFuture<Message> relayMessage(Message messageFromUser, Long serverId, Long channelId, Long modmailThreadId, Member member) {
        Optional<TextChannel> textChannelFromServer = channelService.getTextChannelFromServerOptional(serverId, channelId);
        if(textChannelFromServer.isPresent()) {
            TextChannel textChannel = textChannelFromServer.get();
            return self.sendUserReply(textChannel, modmailThreadId, messageFromUser, member, true);
        } else {
            log.warn("Closing mod mail thread {}, because it seems the channel {} in server {} got deleted.", modmailThreadId, channelId, serverId);
            // in this case there was no text channel on the server associated with the mod mail thread
            // close the existing one, so the user can start a new one
            self.closeModMailThreadInDb(modmailThreadId);
            String textToSend = templateService.renderTemplate("modmail_failed_to_forward_message", new Object());
            return channelService.sendTextToChannel(textToSend, messageFromUser.getChannel());
        }
    }

    /**
     * This message takes a received {@link Message} from a user, renders it to a new message to send and sends it to
     * the appropriate {@link ModMailThread} channel, the returned promise only returns if the message was dealt with on the user
     * side.
     * @param textChannel The {@link TextChannel} in which the {@link ModMailThread} is being handled
     * @param modMailThreadId The id of the modmail thread to which the received {@link Message} is a reply to, can be null, if it is null, its the initial message
     * @param messageFromUser The received message from the user
     * @param member The {@link Member} instance from the user the thread is about. It is used as author
     * @param modMailThreadExists  Whether or not the modmail thread already exists and is persisted.
     * @return A {@link CompletableFuture} which resolves when the post processing of the message is completed (adding read notification, and storing messageIDs)
     */
    public CompletableFuture<Message> sendUserReply(TextChannel textChannel, Long modMailThreadId, Message messageFromUser, Member member, boolean modMailThreadExists) {
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
            log.debug("Mentioning {} subscribers for modmail thread {}.", subscriberList.size(), modMailThreadId);
        } else {
            subscriberMemberFutures.add(CompletableFuture.completedFuture(null));
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
            ModMailUserReplyModel modMailUserReplyModel = ModMailUserReplyModel
                    .builder()
                    .postedMessage(messageFromUser)
                    .member(member)
                    .subscribers(subscribers)
                    .build();
            MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_user_message", modMailUserReplyModel, textChannel.getGuild().getIdLong());
            List<CompletableFuture<Message>> completableFutures = channelService.sendMessageToSendToChannel(messageToSend, textChannel);
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
                    .thenCompose(aVoid -> {
                        log.debug("Adding read reaction to initial message for mod mail thread in channel {}.", textChannel.getGuild().getId());
                        return reactionService.addReactionToMessageAsync("readReaction", textChannel.getGuild().getIdLong(), messageFromUser);
                    })
                    .thenApply(aVoid -> {
                        Message createdMessage = completableFutures.get(0).join();
                        if(modMailThreadExists) {
                            self.postProcessSendMessages(textChannel, createdMessage, messageFromUser);
                        }
                        return messageFuture.complete(createdMessage);
                    }).exceptionally(throwable1 -> {
                        log.error("Failed to forward message to thread.", throwable1);
                        messageFuture.completeExceptionally(throwable1);
                        return null;
                    });
        });
        return messageFuture;

    }

    /**
     * This message handles the post processing of the messages received by the user. This includes: saving the messageIDs
     * in the database, updating the state of the {@link ModMailThread} and adding the read reaction to the user message
     * @param textChannel The channel in which the message
     * @param messageInModMailThread The actual {@link Message} instance which was sent to the mod mail thread
     * @param messageFromUser The {@link Message} object which was sent from the user
     */
    @Transactional
    public void postProcessSendMessages(TextChannel textChannel, Message messageInModMailThread, Message messageFromUser) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getByChannelIdOptional(textChannel.getIdLong());
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            log.debug("Adding created message {} based on messeage {} sent from user to modmail thread {} and setting status to {}.", messageInModMailThread.getId(), messageFromUser.getId(), modMailThread.getId(), ModMailThreadState.USER_REPLIED);
            modMailMessageManagementService.addMessageToThread(modMailThread, null, messageInModMailThread, messageFromUser, modMailThread.getUser(), false, false);
            // update the state of the thread
            modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.USER_REPLIED);
        } else {
            throw new ModMailThreadChannelNotFound();
        }
    }

    @Override
    @Transactional
    public CompletableFuture<Void> relayMessageToDm(Long modmailThreadId, String text, Message replyCommandMessage, boolean anonymous, MessageChannel feedBack, List<UndoActionInstance> undoActions, Member targetMember) {
        log.info("Relaying message {} to user {} in modmail thread {} on server {}.", replyCommandMessage.getId(), targetMember.getId(), modmailThreadId, targetMember.getGuild().getId());
        AUserInAServer moderator = userInServerManagementService.loadOrCreateUser(replyCommandMessage.getMember());
        metricService.incrementCounter(MDOMAIL_THREAD_MESSAGE_SENT);
        ModMailThread modMailThread = modMailThreadManagementService.getById(modmailThreadId);
        FullUserInServer fullThreadUser = FullUserInServer
                .builder()
                .aUserInAServer(modMailThread.getUser())
                .member(targetMember)
                .build();
        ModMailModeratorReplyModel.ModMailModeratorReplyModelBuilder modMailModeratorReplyModelBuilder = ModMailModeratorReplyModel
                .builder()
                .text(text)
                .modMailThread(modMailThread)
                .postedMessage(replyCommandMessage)
                .anonymous(anonymous)
                .threadUser(fullThreadUser);
        if(anonymous) {
            log.debug("Message is sent anonymous.");
            modMailModeratorReplyModelBuilder.moderator(memberService.getBotInGuild(modMailThread.getServer()));
        } else {
            // should be loaded, because we are currently processing a command caused by the message
            Member moderatorMember = memberService.getMemberInServer(moderator);
            modMailModeratorReplyModelBuilder.moderator(moderatorMember);
        }
        ModMailModeratorReplyModel modMailUserReplyModel = modMailModeratorReplyModelBuilder.build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(MODMAIL_STAFF_MESSAGE_TEMPLATE_KEY, modMailUserReplyModel, modMailThread.getServer().getId());
        CompletableFuture<Message> future = messageService.sendMessageToSendToUser(targetMember.getUser(), messageToSend);
        CompletableFuture<Message> sameThreadMessageFuture;
        if(featureModeService.featureModeActive(ModMailFeatureDefinition.MOD_MAIL, modMailThread.getServer(), ModMailMode.SEPARATE_MESSAGE)) {
            sameThreadMessageFuture = channelService.sendMessageEmbedToSendToAChannel(messageToSend, modMailThread.getChannel()).get(0);
        } else {
            sameThreadMessageFuture = CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.allOf(future, sameThreadMessageFuture).thenAccept(avoid ->
                self.saveSendMessagesAndUpdateState(modmailThreadId, anonymous, moderator, future.join(), replyCommandMessage, sameThreadMessageFuture.join())
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
        if(closingConfig.getLog()) {
            if(!modMailMessages.isEmpty()) {
                return modMailMessageService.loadModMailMessages(modMailMessages)
                        .thenAccept(loadedModmailThreadMessages -> self.logMessagesToModMailLog(closingConfig, modMailThreadId, undoActions, loadedModmailThreadMessages, serverId, userId));
            } else {
                log.info("Modmail thread {} in server {} has no messages. Only logging header.", modMailThreadId, serverId);
                return loadUserAndSendClosingHeader(modMailThread, closingConfig)
                        .thenAccept(unused -> memberService.getMemberInServerAsync(modMailThread.getUser()).thenCompose(member ->
                                self.afterSuccessfulLog(modMailThreadId, closingConfig.getNotifyUser(), member, undoActions)
                        ));
            }
        } else {
            log.debug("Not logging modmail thread {}.", modMailThreadId);
            return memberService.getMemberInServerAsync(modMailThread.getUser()).thenCompose(member ->
                self.afterSuccessfulLog(modMailThreadId, closingConfig.getNotifyUser(), member, undoActions)
            );
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
                        undoActions.add(UndoActionInstance.getMessageDeleteAction(message.getGuild().getIdLong(), message.getChannel().getIdLong(), message.getIdLong()));
                    });
                    return memberService.getMemberInServerAsync(serverId, userId).thenCompose(member ->
                            self.afterSuccessfulLog(modMailThreadId, closingContext.getNotifyUser(), member, undoActions)
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
     * @param notifyUser Whether or not the user should be notified
     * @param undoActions The list of {@link UndoActionInstance} to execute in case of exceptions
     * @param modMailThreaduser The {@link Member member} for which the {@link ModMailThread thread} was for
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     * @return A {@link CompletableFuture future} which completes after the messages have been logged
     */
    @Transactional
    public CompletableFuture<Void> afterSuccessfulLog(Long modMailThreadId, Boolean notifyUser, Member modMailThreaduser, List<UndoActionInstance> undoActions) {
        log.debug("Mod mail logging for thread {} has completed. Starting post logging activities.", modMailThreadId);
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getByIdOptional(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            if(notifyUser) {
                log.info("Notifying user about the closed modmail thread {}.", modMailThreadId);
                ModMailThread modMailThread = modMailThreadOpt.get();
                HashMap<String, String> closingMessage = new HashMap<>();
                String defaultValue = templateService.renderSimpleTemplate("modmail_closing_user_message_description");
                closingMessage.put("closingMessage", configService.getStringValue(MODMAIL_CLOSING_MESSAGE_TEXT, modMailThread.getServer().getId(), defaultValue));
                return messageService.sendEmbedToUser(modMailThreaduser.getUser(), "modmail_closing_user_message", closingMessage).thenAccept(message ->
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
                log.debug("Deleting channel {} which contained the modmail thread {}.", modMailThread.getChannel().getId(), modMailThreadId);
                return channelService.deleteTextChannel(modMailThread.getChannel()).thenAccept(avoid -> {
                    undoActions.clear();
                    self.closeModMailThreadInDb(modMailThreadId);
                });
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
        TextChannel channel = channelService.getTextChannelFromServer(serverId, modMailThreadId);
        ClosingProgressModel progressModel = ClosingProgressModel
                .builder()
                .loggedMessages(0)
                .totalMessages(messages.getMessages().size())
                .build();
        List<CompletableFuture<Message>> updateMessageFutures = channelService.sendEmbedTemplateInTextChannelList(MODMAIL_CLOSE_PROGRESS_TEMPLATE_KEY, progressModel, channel);
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
                        .findFirst().orElseThrow(() -> new AbstractoRunTimeException("Could not find desired message in list of messages in thread. This should not happen, as we just retrieved them from the same place."));
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
                .silently(closingContext.getNotifyUser())
                .messageCount(modMailThread.getMessages().size())
                .startDate(modMailThread.getCreated())
                .serverId(modMailThread.getServer().getId())
                .silently(!closingContext.getNotifyUser())
                .userId(modMailThread.getUser().getUserReference().getId())
                .build();
        return userService.retrieveUserForId(modMailThread.getUser().getUserReference().getId()).thenApply(user -> {
            headerModel.setUser(user);
            return self.sendClosingHeader(headerModel).get(0);
        }).thenCompose(Function.identity());
    }

    @Transactional
    public List<CompletableFuture<Message>> sendClosingHeader(ModMailClosingHeaderModel model) {
        MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_close_header", model, model.getServerId());
        return postTargetService.sendEmbedInPostTarget(messageToSend, ModMailPostTargets.MOD_MAIL_LOG, model.getServerId());
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
     * @return A list of {@link CompletableFuture} which represent each of the messages being send to the {@link PostTarget}
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
     * @param moderator The original {@link AUserInAServer} which authored the messages
     * @param createdMessageInDM The {@link Message message} which was sent to the private channel with the {@link User user}
     * @param modMailThreadMessage The {@link Message message} which was sent in the channel representing the {@link ModMailThread thread}. Might be null.
     * @param replyCommandMessage The {@link Message message} which contained the command used to reply to the user
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     */
    @Transactional
    public void saveSendMessagesAndUpdateState(Long modMailThreadId, Boolean anonymous, AUserInAServer moderator, Message createdMessageInDM, Message replyCommandMessage, Message modMailThreadMessage) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getByIdOptional(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            log.debug("Adding (anonymous: {}) message {} of moderator to modmail thread {} and setting state to {}.", anonymous, createdMessageInDM.getId(), modMailThreadId, ModMailThreadState.MOD_REPLIED);
            modMailMessageManagementService.addMessageToThread(modMailThread, createdMessageInDM, modMailThreadMessage, replyCommandMessage, moderator, anonymous, true);
            modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.MOD_REPLIED);
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(MODMAIL_THREAD_CREATED_COUNTER, "Mod mail threads created");
        metricService.registerCounter(MODMAIL_THREAD_CLOSED_COUNTER, "Mod mail threads closed");
        metricService.registerCounter(MDOMAIL_THREAD_MESSAGE_RECEIVED, "Mod mail messages received");
        metricService.registerCounter(MDOMAIL_THREAD_MESSAGE_SENT, "Mod mail messages sent");
    }
}
