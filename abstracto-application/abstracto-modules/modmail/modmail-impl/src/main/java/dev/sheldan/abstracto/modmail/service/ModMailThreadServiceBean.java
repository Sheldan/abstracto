package dev.sheldan.abstracto.modmail.service;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.exception.PostTargetNotFoundException;
import dev.sheldan.abstracto.core.exception.UserInServerNotFoundException;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.FullGuild;
import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.UndoActionInstance;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.modmail.config.*;
import dev.sheldan.abstracto.modmail.exception.ModMailCategoryIdException;
import dev.sheldan.abstracto.modmail.exception.ModMailThreadNotFoundException;
import dev.sheldan.abstracto.modmail.models.database.*;
import dev.sheldan.abstracto.modmail.models.dto.ServerChoice;
import dev.sheldan.abstracto.modmail.models.template.*;
import dev.sheldan.abstracto.modmail.service.management.ModMailMessageManagementService;
import dev.sheldan.abstracto.modmail.service.management.ModMailRoleManagementService;
import dev.sheldan.abstracto.modmail.service.management.ModMailSubscriberManagementService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import dev.sheldan.abstracto.modmail.validator.ModMailFeatureValidator;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    /**
     * The template key used for default mod mail exceptions
     */
    public static final String MODMAIL_EXCEPTION_GENERIC_TEMPLATE = "modmail_exception_generic";

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
    private BotService botService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ModMailMessageManagementService modMailMessageManagementService;

    @Autowired
    private ModMailMessageService modMailMessageService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserInServerService userInServerService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ModMailFeature modMailFeature;

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
    private ModMailThreadServiceBean self;

    /**
     * The emoji used when the user can decide for a server to open a mod mail thread in.
     */
    private List<String> NUMBER_EMOJI = Arrays.asList("\u0031\u20e3", "\u0032\u20e3", "\u0033\u20e3",
            "\u0034\u20e3", "\u0035\u20e3", "\u0036\u20e3",
            "\u0037\u20e3", "\u0038\u20e3", "\u0039\u20e3",
            "\u0040\u20e3");


    @Override
    public void createModMailThreadForUser(FullUser aUserInAServer, Message initialMessage, MessageChannel feedBackChannel, boolean userInitiated) {
        Long serverId = aUserInAServer.getAUserInAServer().getServerReference().getId();
        Long categoryId = configService.getLongValue(MODMAIL_CATEGORY, serverId);
        User user = aUserInAServer.getMember().getUser();
        CompletableFuture<TextChannel> textChannelFuture = channelService.createTextChannel(user.getName() + user.getDiscriminator(), aUserInAServer.getAUserInAServer().getServerReference(), categoryId);

        Long userInServerId = aUserInAServer.getAUserInAServer().getUserInServerId();
        textChannelFuture.thenAccept(channel -> {
            List<UndoActionInstance> undoActions = new ArrayList<>();
            undoActions.add(UndoActionInstance.getChannelDeleteAction(serverId, channel.getIdLong()));
            self.performModMailThreadSetup(aUserInAServer, initialMessage, channel, userInitiated, undoActions);
        }).exceptionally(throwable -> {
            log.error("Failed to create mod mail thread", throwable);
            sendModMailFailure("modmail_exception_failed_to_create_mod_mail_thread", userInServerId, null, feedBackChannel, throwable);
            return null;
        });
    }

    /**
     * this method is responsible for creating the instance in the database, sending the header in the newly created text channel and forwarding the initial message
     * by the user (if any), after this is complete, this method executes the method to perform the mod mail notification.
     * @param aUserInAServer The {@link FullUser} for which a {@link ModMailThread} is being created
     * @param initialMessage The {@link Message} which was sent by the user to open a thread, this is null, if the thread was oepend via a command
     * @param channel The created {@link TextChannel} in which the mod mail thread is dealth with
     * @param userInitiated Whether or not the thread was initiated by a member
     * @param undoActions The list of actions to undo, in case an exception occurs
     */
    @Transactional
    public void performModMailThreadSetup(FullUser aUserInAServer, Message initialMessage, TextChannel channel, boolean userInitiated, List<UndoActionInstance> undoActions) {
        try {
            ModMailThread thread = createThreadObject(channel, aUserInAServer);
            sendModMailHeader(channel, aUserInAServer, undoActions);
            CompletableFuture<Void> future;
            if(initialMessage != null){
                future = self.sendUserReply(channel, thread, initialMessage);
            } else {
                future = CompletableFuture.completedFuture(null);
            }
            future.thenAccept(aVoid -> {
                if(userInitiated) {
                    self.sendModMailNotification(aUserInAServer, thread, undoActions);
                }
            });
        } catch (Exception e) {
            log.error("Failed to perform mod mail thread setup.", e);
            undoActionService.performActions(undoActions);
        }
    }

    /**
     * Sends the message containing the pings to notify the staff members to handle the opened {@link ModMailThread}
     * @param aUserInAServer The {@link FullUser} which opened the thread
     * @param thread The {@link ModMailThread} instance which was created
     * @param undoActions The list of {@link UndoActionInstance} to perform, in case an exception occurs
     */
    @Transactional
    public void sendModMailNotification(FullUser aUserInAServer, ModMailThread thread, List<UndoActionInstance> undoActions) {
        List<ModMailRole> rolesToPing = modMailRoleManagementService.getRolesForServer(thread.getServer());
        ModMailNotificationModel modMailNotificationModel = ModMailNotificationModel
                .builder()
                .modMailThread(thread)
                .threadUser(aUserInAServer)
                .roles(rolesToPing)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_notification_message", modMailNotificationModel);
        List<CompletableFuture<Message>> modmailping = postTargetService.sendEmbedInPostTarget(messageToSend, ModMailPostTargets.MOD_MAIL_PING, thread.getServer().getId());
        CompletableFuture.allOf(modmailping.toArray(new CompletableFuture[0])).whenComplete((aVoid, throwable) -> {
            if(throwable != null) {
                log.error("Failed to send mod mail thread notification ping.", throwable);
                undoActionService.performActions(undoActions);
            }
        });
    }

    /**
     * Creates the instance of the {@link ModMailThread} in the database.
     * @param channel The {@link TextChannel} in which the {@link ModMailThread} is being done
     * @param user The {@link FullUser} which the thread is about
     * @return The created instance of {@link ModMailThread}
     */
    public ModMailThread createThreadObject(TextChannel channel, FullUser user) {
        AChannel channel2 = channelManagementService.createChannel(channel.getIdLong(), AChannelType.TEXT, user.getAUserInAServer().getServerReference());
        log.info("Creating mod mail thread in channel {} with db channel {}", channel.getIdLong(), channel2.getId());
        return modMailThreadManagementService.createModMailThread(user.getAUserInAServer(), channel2);
    }

    @Override
    public void setModMailCategoryTo(Guild guild, Long categoryId) {
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
        // do nothing if we dont know the user
        if(!knownServers.isEmpty()) {
            List<ServerChoice> availableGuilds = new ArrayList<>();
            HashMap<String, AUserInAServer> choices = new HashMap<>();
            for (int i = 0; i < knownServers.size(); i++) {
                AUserInAServer aUserInAServer = knownServers.get(i);
                // only take the servers in which mod mail is actually enabled, would not make much sense to make the
                // other servers available
                if(featureFlagService.isFeatureEnabled(modMailFeature, aUserInAServer.getServerReference())) {
                    AServer serverReference = aUserInAServer.getServerReference();
                    FullGuild guild = FullGuild
                            .builder()
                            .guild(botService.getGuildByIdNullable(serverReference.getId()))
                            .server(serverReference)
                            .build();
                    // TODO support more than this limited amount of servers
                    String reactionEmote = NUMBER_EMOJI.get(i);
                    ServerChoice serverChoice = ServerChoice.builder().guild(guild).reactionEmote(reactionEmote).build();
                    choices.put(reactionEmote, aUserInAServer);
                    availableGuilds.add(serverChoice);
                }
            }
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
                            AUserInAServer chosenServer = choices.get(reactionEmote.getEmoji());
                            Member memberInServer = botService.getMemberInServer(chosenServer);
                            FullUser fullUser = FullUser.builder().member(memberInServer).aUserInAServer(chosenServer).build();
                            self.createModMailThreadForUser(fullUser, initialMessage, initialMessage.getChannel(), true);
                        })
                        .build();
                menu.display(initialMessage.getChannel());
            } else if(availableGuilds.size() == 1) {
                // if exactly one server is available, open the thread directly
                AUserInAServer chosenServer = choices.get(availableGuilds.get(0).getReactionEmote());
                Member memberInServer = botService.getMemberInServer(chosenServer);
                FullUser fullUser = FullUser.builder().member(memberInServer).aUserInAServer(chosenServer).build();
                self.createModMailThreadForUser(fullUser, initialMessage, initialMessage.getChannel(), true);
            } else {
                // in case there is no server available, send an error message
                channelService.sendEmbedTemplateInChannel("modmail_no_server_available", new Object(), initialMessage.getChannel());
            }
        } else {
            log.warn("User which was not known in any of the servers tried to contact the bot. {}", user.getId());
        }
    }


    /**
     * Method used to send the header of a newly created mod mail thread. This message contains information about
     * the user which the thread is about
     * @param channel The {@link TextChannel} in which the mod mail thread is present in
     * @param aUserInAServer The {@link AUserInAServer} which the {@link ModMailThread} is about
     * @param undoActions The list of {@link UndoActionInstance} to execute in case an exception occurs
     */
    private void sendModMailHeader(TextChannel channel, FullUser aUserInAServer, List<UndoActionInstance> undoActions) {
        ModMailThread latestThread = modMailThreadManagementService.getLatestModMailThread(aUserInAServer.getAUserInAServer());
        List<ModMailThread> oldThreads = modMailThreadManagementService.getModMailThreadForUser(aUserInAServer.getAUserInAServer());
        ModMailThreaderHeader header = ModMailThreaderHeader
                .builder()
                .threadUser(aUserInAServer)
                .latestModMailThread(latestThread)
                .pastModMailThreadCount((long)oldThreads.size())
                .build();
        List<CompletableFuture<Message>> messages = channelService.sendEmbedTemplateInChannel("modmail_thread_header", header, channel);
        CompletableFuture.allOf(messages.toArray(new CompletableFuture[0])).whenComplete((aVoid, throwable)-> {
            if(throwable != null) {
                log.error("Failed to send mod mail header for for user {}.", aUserInAServer.getAUserInAServer().getUserReference().getId(), throwable);
                undoActionService.performActions(undoActions);
            }
        });
    }

    @Override
    public void relayMessageToModMailThread(ModMailThread modMailThread, Message message) {
        Optional<TextChannel> textChannelFromServer = botService.getTextChannelFromServer(modMailThread.getServer().getId(), modMailThread.getChannel().getId());
        if(textChannelFromServer.isPresent()) {
            TextChannel textChannel = textChannelFromServer.get();
            self.sendUserReply(textChannel, modMailThread, message);
        } else {
            // in this case there was no text channel on the server associated with the mod mail thread
            // close the existing one, so the user can start a new one
            message.getChannel().sendMessage(templateService.renderTemplate("modmail_failed_to_forward_message", new Object())).queue();
            self.closeModMailThreadInDb(modMailThread.getId());
        }
    }

    /**
     * This message takes a received {@link Message} from a user, renders it to a new message to send and sends it to
     * the appropriate {@link ModMailThread} channel, the returned promise only returns if the message was dealt with on the user
     * side.
     * @param textChannel The {@link TextChannel} in which the {@link ModMailThread} is being handled
     * @param modMailThread The {@link ModMailThread} to which the received {@link Message} is a reply to
     * @param message The received message from the user
     * @return A {@link CompletableFuture} which resolves when the post processing of the message is completed (adding read notification, and storing messageIDs)
     */
    public CompletableFuture<Void> sendUserReply(TextChannel textChannel, ModMailThread modMailThread, Message message) {
        Long modMailThreadId = modMailThread.getId();
        FullUser fullUser = FullUser
                .builder()
                .aUserInAServer(modMailThread.getUser())
                .member(botService.getMemberInServer(modMailThread.getUser()))
                .build();

        List<FullUser> subscribers = new ArrayList<>();
        List<ModMailThreadSubscriber> subscriberList = modMailSubscriberManagementService.getSubscribersForThread(modMailThread);
        subscriberList.forEach(modMailThreadSubscriber -> {
            FullUser subscriber = FullUser
                    .builder()
                    .aUserInAServer(modMailThreadSubscriber.getSubscriber())
                    .member(botService.getMemberInServer(modMailThreadSubscriber.getSubscriber()))
                    .build();
            subscribers.add(subscriber);
        });
        ModMailUserReplyModel modMailUserReplyModel = ModMailUserReplyModel
                .builder()
                .modMailThread(modMailThread)
                .postedMessage(message)
                .threadUser(fullUser)
                .subscribers(subscribers)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_user_message", modMailUserReplyModel);
        List<CompletableFuture<Message>> completableFutures = channelService.sendMessageToSendToChannel(messageToSend, textChannel);
        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenAccept(aVoid ->
            self.postProcessSendMessages(modMailThreadId, message, completableFutures)
        );

    }

    /**
     * This message handles the post processing of the messages received by the user. This includes: saving the messageIDs
     * in the database, updating the state of the {@link ModMailThread} and adding the read reaction to the user message
     * @param modMailThreadId The ID of the {@link ModMailThread} for which the message was directed to
     * @param message The actual {@link Message} instance received from the user.
     * @param completableFutures The list of {@link CompletableFuture} which were rendered from the sent message
     *                           and posted in the {@link ModMailThread} by Abstracto
     */
    @Transactional
    public void postProcessSendMessages(Long modMailThreadId, Message message, List<CompletableFuture<Message>> completableFutures) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            List<Message> messages = new ArrayList<>();
            completableFutures.forEach(messageCompletableFuture -> {
                try {
                    Message messageToAdd = messageCompletableFuture.get();
                    messages.add(messageToAdd);
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error while executing future to retrieve reaction.", e);
                    Thread.currentThread().interrupt();
                }
                self.saveMessageIds(messages, modMailThread, modMailThread.getUser(), false, false);
                // update the state of the thread
                modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.USER_REPLIED);
                // add the reaction to show that the message has been processed
                messageService.addReactionToMessage("readReaction", modMailThread.getServer().getId(), message);
            });
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }
    }

    @Override
    public void relayMessageToDm(ModMailThread modMailThread, String text, Message message, boolean anonymous, MessageChannel feedBack) {
        Long modMailThreadId = modMailThread.getId();
        User userById = botService.getInstance().getUserById(modMailThread.getUser().getUserReference().getId());
        if(userById != null) {
            userById.openPrivateChannel().queue(privateChannel ->
                self.sendReply(modMailThreadId, text, message, privateChannel, anonymous, feedBack)
            ,throwable ->
                log.warn("Failed to open private channel with user {}", userById.getIdLong())
            );
        }
    }

    /**
     * Notifies the staff members in the thread about any exception occurring when executing a command in a {@link ModMailThread}.
     * This takes a custom template which is rendered and a generic model is provided.
     * @param template The key of the {@link dev.sheldan.abstracto.templating.model.database.Template} to use
     * @param userInServerId The ID of the user in the server which the {@link ModMailThread} is about
     * @param modMailTreadId The ID of the {@link ModMailThread} in which an exception occurred
     * @param channel The {@link MessageChannel} in to which the exception message should be sent to
     * @param throwable The instance of the {@link Throwable} which happened.
     * @throws UserInServerNotFoundException in case the {@link AUserInAServer} was not found by the ID
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} was not found by the ID
     */
    @Transactional
    public void sendModMailFailure(String template, Long userInServerId,  Long modMailTreadId, MessageChannel channel, Throwable throwable) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailTreadId);
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(userInServerId).orElseThrow(() -> new UserInServerNotFoundException(userInServerId));
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            try {
                FullUser fullUser = FullUser
                        .builder()
                        .aUserInAServer(aUserInAServer)
                        .member(botService.getMemberInServer(aUserInAServer))
                        .build();
                ModMailExceptionModel modMailExceptionModel = ModMailExceptionModel
                        .builder()
                        .modMailThread(modMailThread)
                        .user(fullUser)
                        .throwable(throwable)
                        .build();
                channelService.sendEmbedTemplateInChannel(template, modMailExceptionModel, channel);
            } catch (Exception e) {
                log.error("Failed to notify about mod mail exception.", e);
            }
        } else {
            throw new ModMailThreadNotFoundException(modMailTreadId);
        }
    }

    @Override
    public void closeModMailThread(ModMailThread modMailThread, MessageChannel feedBack, String note, boolean notifyUser) {
        AFeatureMode aFeatureMode = featureModeService.getFeatureMode(ModMailFeatures.MOD_MAIL, modMailThread.getServer());
        boolean loggingMode = aFeatureMode.getMode().equalsIgnoreCase(ModMailMode.LOGGING.getKey());
        closeModMailThread(modMailThread, feedBack, note, notifyUser, loggingMode);
    }

    @Override
    public void closeModMailThread(ModMailThread modMailThread, MessageChannel feedBack, String note, boolean notifyUser, boolean logThread) {
        Long modMailThreadId = modMailThread.getId();
        log.info("Starting closing procedure for thread {}", modMailThread.getId());
        List<ModMailMessage> modMailMessages = modMailThread.getMessages();
        List<UndoActionInstance> undoActions = new ArrayList<>();
        if(logThread) {
            List<CompletableFuture<Message>> messages = modMailMessageService.loadModMailMessages(modMailMessages);
            log.trace("Loading {} mod mail thread messages.", messages.size());
            for (int i = 0; i < messages.size(); i++) {
                CompletableFuture<Message> messageCompletableFuture = messages.get(i);
                Long messageId = modMailMessages.get(i).getMessageId();
                messageCompletableFuture.exceptionally(throwable -> {
                    log.warn("Failed to load message {} in mod mail thread {}", messageId, modMailThreadId);
                    return null;
                });
            }
            CompletableFuture.allOf(messages.toArray(new CompletableFuture[0])).whenComplete((avoid, throwable) -> {
                Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailThreadId);
                if(modMailThreadOpt.isPresent()) {
                    ModMailThread innerModMailThread = modMailThreadOpt.get();
                    log.trace("Loaded {} mod mail thread messages", messages.size());
                    if(throwable != null) {
                        log.warn("Failed to load some mod mail messages for mod mail thread {}. Still trying to post the ones we got.", modMailThreadId, throwable);
                    }
                    logMessagesToModMailLog(feedBack, note, notifyUser, modMailThreadId, undoActions, messages, innerModMailThread);
                } else {
                    throw new ModMailThreadNotFoundException(modMailThreadId);
                }
            });
        } else {
            self.afterSuccessfulLog(modMailThreadId, feedBack, notifyUser, undoActions);
        }
    }

    /**
     * This method takes the actively loaded futures, calls the method responsible for logging the messages, and calls the method
     * after the logging has been done.
     * @param feedBack The {@link MessageChannel} in which possible feedback about exceptions is sent to
     * @param note The note which was provided when closing the {@link ModMailThread}
     * @param notifyUser Whether or not to notify the user
     * @param modMailThreadId The ID of the {@link ModMailThread} which is being closed
     * @param undoActions The list of {@link UndoActionInstance} to execute in case of exceptions
     * @param messages The list of loaded {@link Message} to log
     * @param innerModMailThread An instance of {@link ModMailThread} which is getting closed
     */
    private void logMessagesToModMailLog(MessageChannel feedBack, String note, Boolean notifyUser, Long modMailThreadId, List<UndoActionInstance> undoActions, List<CompletableFuture<Message>> messages, ModMailThread innerModMailThread) {
        Long userInServerId = innerModMailThread.getUser().getUserInServerId();
        try {
            CompletableFutureList<Message> list = self.logModMailThread(modMailThreadId, messages, note);
            list.getMainFuture().thenRun(() -> {
                list.getFutures().forEach(messageCompletableFuture -> {
                    try {
                        Message message = messageCompletableFuture.get();
                        undoActions.add(UndoActionInstance.getMessageDeleteAction(message.getChannel().getIdLong(), message.getIdLong()));
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Failed to post logging messages.", e);
                        Thread.currentThread().interrupt();
                    }  catch (Exception e) {
                        log.error("Failed to handle the mod mail log messages.", e);
                    }
                });
                self.afterSuccessfulLog(modMailThreadId, feedBack, notifyUser, undoActions);
            });
            list.getMainFuture().exceptionally(innerThrowable -> {
                sendModMailFailure(MODMAIL_EXCEPTION_GENERIC_TEMPLATE, userInServerId, modMailThreadId, feedBack, innerThrowable);
                log.error("Failed to log messages for mod mail thread {}.", modMailThreadId, innerThrowable);
                return null;
            });
        } catch (PostTargetNotFoundException po) {
            log.error("Failed to log mod mail messages", po);
            sendModMailFailure("modmail_exception_post_target_not_defined", userInServerId, modMailThreadId, feedBack, po);
        } catch (Exception e) {
            log.error("Failed to log mod mail messages", e);
            sendModMailFailure(MODMAIL_EXCEPTION_GENERIC_TEMPLATE, userInServerId, modMailThreadId, feedBack, e);
        }
    }

    /**
     * This message is executed after the thread has been logged and notifies the user about the closed {@link ModMailThread}
     * which a configurable closing text. This method then calls the method to delete the channel.
     * @param modMailThreadId The ID of the {@link ModMailThread} which is being closed.
     * @param feedBack The {@link MessageChannel} in which exceptions should be sent to
     * @param notifyUser Whether or not the user should be notified
     * @param undoActions The list of {@link UndoActionInstance} to execute in case of exceptions
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     */
    @Transactional
    public void afterSuccessfulLog(Long modMailThreadId, MessageChannel feedBack, Boolean notifyUser,  List<UndoActionInstance> undoActions) {
        log.trace("Mod mail logging for thread {} has completed. Starting post logging activities.", modMailThreadId);
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            User user = botService.getMemberInServer(modMailThread.getUser()).getUser();
            user.openPrivateChannel().queue(privateChannel ->  {
                try {
                    List<CompletableFuture<Message>> messageFutures = new ArrayList<>();
                    if(notifyUser){
                        log.trace("Notifying user {}", user.getIdLong());
                        HashMap<String, String> closingMessage = new HashMap<>();
                        String defaultValue = templateService.renderSimpleTemplate("modmail_closing_user_message_description");
                        closingMessage.put("closingMessage", configService.getStringValue(MODMAIL_CLOSING_MESSAGE_TEXT, modMailThread.getServer().getId(), defaultValue));
                        messageFutures.addAll(channelService.sendEmbedTemplateInChannel("modmail_closing_user_message", closingMessage , privateChannel));
                    } else {
                        log.trace("*Not* notifying user {}", user.getIdLong());
                        messageFutures.add(CompletableFuture.completedFuture(null));
                    }
                    CompletableFuture.allOf(messageFutures.toArray(new CompletableFuture[0])).whenComplete((result, throwable) -> {
                        if(throwable != null) {
                            log.warn("Failed to send closing message to user {} after closing mod mail thread {}", user.getIdLong(), modMailThread.getId(), throwable);
                        }
                        self.deleteChannelAndClose(modMailThreadId, feedBack, undoActions);
                    });
                } catch (Exception e) {
                    log.error("Failed to render closing user message", e);
                    Long userInServerId = modMailThread.getUser().getUserInServerId();
                    sendModMailFailure(MODMAIL_EXCEPTION_GENERIC_TEMPLATE, userInServerId, modMailThreadId, feedBack, e);
                }
            }, throwable -> {
                log.error("Failed to load private channel with user {}", user.getIdLong(), throwable);
                undoActionService.performActions(undoActions);
            });
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }
    }

    /**
     * Deletes the actual {@link MessageChannel} in which the {@link ModMailThread} happened. This method then calls the
     * method to update the stats in the database
     * @param modMailThreadId The ID of the {@link ModMailThread} to delete the {@link MessageChannel} from
     * @param feedBack The {@link MessageChannel} in which exceptions should be sent to
     * @param undoActions The list of {@link UndoActionInstance} to execute in case of exceptions
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     */
    @Transactional
    public void deleteChannelAndClose(Long modMailThreadId, MessageChannel feedBack, List<UndoActionInstance> undoActions) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            String failureMessage = "Failed to delete text channel containing mod mail thread {}";
            try {
                channelService.deleteTextChannel(modMailThread.getChannel()).thenRun(() -> {
                    try {
                        self.closeModMailThreadInDb(modMailThreadId);
                    } catch (Exception e) {
                        undoActionService.performActions(undoActions);
                    }
                }).exceptionally(throwable2 -> {
                    undoActionService.performActions(undoActions);
                    log.error(failureMessage, modMailThread.getId(), throwable2);
                    return null;
                });
            } catch (InsufficientPermissionException ex) {
                log.error(failureMessage, modMailThreadId, ex);
                undoActionService.performActions(undoActions);
                sendModMailFailure("modmail_exception_cannot_delete_channel", modMailThread.getUser().getUserInServerId(), modMailThreadId, feedBack, ex);
            } catch (Exception ex) {
                log.error(failureMessage, modMailThreadId, ex);
                undoActionService.performActions(undoActions);
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
     * @param note The note which was entered when closing the {@link ModMailThread}
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     * @return An instance of {@link CompletableFutureList}, which contains a main {@link CompletableFuture} which is resolved,
     * when all of the smaller {@link CompletableFuture} in it are resolved. We need this construct, because we need to access
     * the result values of the individual futures after they are done.
     */
    @Transactional
    public CompletableFutureList<Message> logModMailThread(Long modMailThreadId, List<CompletableFuture<Message>> messages, String note) {
        log.info("Logging mod mail thread {}.", modMailThreadId);
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            List<ModMailLoggedMessageModel> loggedMessages = new ArrayList<>();
            messages.forEach(future -> {
                try {
                    if(!future.isCompletedExceptionally()) {
                        Message loadedMessage = future.get();
                        if(loadedMessage != null) {
                            ModMailMessage modmailMessage = modMailThread.getMessages()
                                    .stream()
                                    .filter(modMailMessage -> modMailMessage.getMessageId().equals(loadedMessage.getIdLong()))
                                    .findFirst().orElseThrow(() -> new AbstractoRunTimeException("Could not find desired message in list of messages in thread. This should not happen, as we just retrieved them from the same place."));
                            ModMailLoggedMessageModel modMailLoggedMessageModel =
                                    ModMailLoggedMessageModel
                                            .builder()
                                            .message(loadedMessage)
                                            .modMailMessage(modmailMessage)
                                            .author(userInServerService.getFullUser(modmailMessage.getAuthor()))
                                            .build();
                            loggedMessages.add(modMailLoggedMessageModel);
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error while executing future to retrieve reaction.", e);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("Failed handle the loaded messages.", e);
                }
            });
            List<CompletableFuture<Message>> completableFutures = new ArrayList<>();
            modMailThread.setClosed(Instant.now());
            ModMailClosingHeaderModel headerModel = ModMailClosingHeaderModel
                    .builder()
                    .closedThread(modMailThread)
                    .note(note)
                    .build();
            log.trace("Sending close header and individual mod mail messages to mod mail log target.");
            MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_close_header", headerModel);
            List<CompletableFuture<Message>> closeHeaderFutures = postTargetService.sendEmbedInPostTarget(messageToSend, ModMailPostTargets.MOD_MAIL_LOG, modMailThread.getServer().getId());
            completableFutures.addAll(closeHeaderFutures);
            completableFutures.addAll(self.sendMessagesToPostTarget(modMailThread, loggedMessages));
            CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
            return CompletableFutureList
                    .<Message>builder()
                    .mainFuture(voidCompletableFuture)
                    .futures(completableFutures)
                    .build();
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }
    }

    /**
     * Sets the {@link ModMailThread} in the database to CLOSED.
     * @param modMailThreadId The ID of the {@link ModMailThread} to update the state of
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     */
    @Transactional
    public void closeModMailThreadInDb(Long modMailThreadId) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            log.info("Setting thread {} to closed in db.", modMailThread.getId());
            modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.CLOSED);
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }
    }

    /**
     * Renders the retrieved {@link Message} which are in {@link ModMailLoggedMessageModel} into {@link MessageToSend} and
     * sends this to the appropriate logging {@link PostTarget}
     * @param modMailThread The {@link ModMailThread} to which the loaded messages belong to
     * @param loadedMessages The list of {@link ModMailLoggedMessageModel} which can be rendered
     * @return A list of {@link CompletableFuture} which represent each of the messages being send to the {@link PostTarget}
     */
    public List<CompletableFuture<Message>> sendMessagesToPostTarget(ModMailThread modMailThread, List<ModMailLoggedMessageModel> loadedMessages) {
        List<CompletableFuture<Message>> messageFutures = new ArrayList<>();
        loadedMessages.forEach(message -> {
            MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_close_logged_message", message);
            List<CompletableFuture<Message>> logFuture = postTargetService.sendEmbedInPostTarget(messageToSend, ModMailPostTargets.MOD_MAIL_LOG, modMailThread.getServer().getId());
            messageFutures.addAll(logFuture);
        });
        return messageFutures;
    }

    /**
     * Sends the reply which was done by a staff member to the private channel with the {@link Member} and calls the
     * method for saving the messages and updating the status
     * @param modMailThreadId The ID of the {@link ModMailThread} for which the reply was created for
     * @param text The text the reply should contain
     * @param message The original message which triggered the command to create the reply. This is necessary for attachments
     * @param privateChannel The instance of a {@link PrivateChannel} which was opened with the user and is used to send messages
     * @param anonymous Whether or not the reply should be anonymous
     * @param feedBack The {@link MessageChannel} in which exceptions should be sent to
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     */
    @Transactional
    public void sendReply(Long modMailThreadId, String text, Message message, PrivateChannel privateChannel, Boolean anonymous, MessageChannel feedBack) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            AUserInAServer moderator = userInServerManagementService.loadUser(message.getMember());
            Member userInGuild = botService.getMemberInServer(modMailThread.getUser());
            FullUser fullThreadUser = FullUser
                    .builder()
                    .aUserInAServer(modMailThread.getUser())
                    .member(userInGuild)
                    .build();
            ModMailModeratorReplyModel.ModMailModeratorReplyModelBuilder modMailModeratorReplyModelBuilder = ModMailModeratorReplyModel
                    .builder()
                    .text(text)
                    .modMailThread(modMailThread)
                    .postedMessage(message)
                    .anonymous(anonymous)
                    .threadUser(fullThreadUser);
            if(anonymous) {
                modMailModeratorReplyModelBuilder.moderator(botService.getBotInGuild(modMailThread.getServer()));
            } else {
                Member moderatorMember = botService.getMemberInServer(moderator);
                modMailModeratorReplyModelBuilder.moderator(moderatorMember);
            }
            ModMailModeratorReplyModel modMailUserReplyModel = modMailModeratorReplyModelBuilder.build();
            MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_staff_message", modMailUserReplyModel);
            Long userInServerId = modMailThread.getUser().getUserInServerId();
            List<CompletableFuture<Message>> completableFutures = channelService.sendMessageToSendToChannel(messageToSend, privateChannel);
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenAccept(aVoid ->
                self.saveSendMessagesAndUpdateState(modMailThreadId, anonymous, moderator, completableFutures)
            ).exceptionally(throwable -> {
                log.error("Failed to send message to user in server {}", userInServerId);
                sendModMailFailure("modmail_exception_cannot_message_user", userInServerId, modMailThread.getId(), feedBack, throwable);
                return null;
            });
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }

    }

    /**
     * Evaluates the promises which were created when sending the messages to the private channel and stores the message IDs
     * and updates the state of the {@link ModMailThread}.
     * @param modMailThreadId The ID of the {@link ModMailThread} for which the messages were sent for
     * @param anonymous Whether or not the messages were send anonymous
     * @param moderator The original {@link AUserInAServer} which authored the messages
     * @param completableFutures The list of {@link CompletableFuture} which contain the {@link Message} which were sent to the member
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     */
    @Transactional
    public void saveSendMessagesAndUpdateState(Long modMailThreadId, Boolean anonymous, AUserInAServer moderator, List<CompletableFuture<Message>> completableFutures) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            List<Message> messages = new ArrayList<>();
            completableFutures.forEach(messageCompletableFuture -> {
                try {
                    Message messageToAdd = messageCompletableFuture.get();
                    messages.add(messageToAdd);
                } catch (InterruptedException | ExecutionException e) {
                    log.error("A future when sending the message to the user was interrupted.", e);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("Failed to handle the send staff message.", e);
                }
            });
            self.saveMessageIds(messages, modMailThread, moderator, anonymous, true);
            modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.MOD_REPLIED);
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }
    }

    /**
     * Takes the list of {@link Message} and attaches them to the given {@link ModMailThread} as {@link ModMailMessage} which should possibly
     * be logged at a later time
     * @param messages The list of {@link Message} to store the IDs of
     * @param modMailThread The {@link ModMailThread} which should have the messages attached
     * @param author The {@link AUserInAServer} who authored the {@link Message}
     * @param anonymous Whether or not the {@link Message} was anonymous
     * @param inDmChannel Whether or not the {@link Message} was sent in a private channel
     */
    public void saveMessageIds(List<Message> messages, ModMailThread modMailThread, AUserInAServer author, Boolean anonymous, Boolean inDmChannel) {
        messages.forEach(message ->
            modMailMessageManagementService.addMessageToThread(modMailThread, message, author, anonymous, inDmChannel)
        );
    }
}
