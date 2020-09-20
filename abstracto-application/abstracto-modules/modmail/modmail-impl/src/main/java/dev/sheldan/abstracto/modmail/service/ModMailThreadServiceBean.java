package dev.sheldan.abstracto.modmail.service;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.FullGuild;
import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.UndoActionInstance;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
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
    public CompletableFuture<Void> createModMailThreadForUser(FullUserInServer aUserInAServer, Message initialMessage, MessageChannel feedBackChannel, boolean userInitiated, List<UndoActionInstance> undoActions) {
        Long serverId = aUserInAServer.getAUserInAServer().getServerReference().getId();
        Long categoryId = configService.getLongValue(MODMAIL_CATEGORY, serverId);
        User user = aUserInAServer.getMember().getUser();
        CompletableFuture<TextChannel> textChannelFuture = channelService.createTextChannel(user.getName() + user.getDiscriminator(), aUserInAServer.getAUserInAServer().getServerReference(), categoryId);

        return textChannelFuture.thenCompose(channel -> {
            undoActions.add(UndoActionInstance.getChannelDeleteAction(serverId, channel.getIdLong()));
            return self.performModMailThreadSetup(aUserInAServer, initialMessage, channel, userInitiated, undoActions);
        });
    }

    /**
     * This method is responsible for creating the instance in the database, sending the header in the newly created text channel and forwarding the initial message
     * by the user (if any), after this is complete, this method executes the method to perform the mod mail notification.
     * @param aUserInAServer The {@link FullUserInServer} for which a {@link ModMailThread} is being created
     * @param initialMessage The {@link Message} which was sent by the user to open a thread, this is null, if the thread was opened via a command
     * @param channel The created {@link TextChannel} in which the mod mail thread is dealt with
     * @param userInitiated Whether or not the thread was initiated by a member
     * @param undoActions The list of actions to undo, in case an exception occurs
     */
    @Transactional
    public CompletableFuture<Void> performModMailThreadSetup(FullUserInServer aUserInAServer, Message initialMessage, TextChannel channel, boolean userInitiated, List<UndoActionInstance> undoActions) {
        Long userInServerId = aUserInAServer.getAUserInAServer().getUserInServerId();
        CompletableFuture<Void> headerFuture = sendModMailHeader(channel, aUserInAServer);
        CompletableFuture<Void> userReplyMessage;
        if(initialMessage != null){
            userReplyMessage = self.sendUserReply(channel, null, initialMessage, aUserInAServer.getAUserInAServer());
        } else {
            userReplyMessage = CompletableFuture.completedFuture(null);
        }
        CompletableFuture notificationFuture;
        if (userInitiated) {
            notificationFuture = self.sendModMailNotification(aUserInAServer);
        } else {
            notificationFuture = CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.allOf(headerFuture, notificationFuture, userReplyMessage).thenAccept(aVoid -> {
            undoActions.clear();
            self.setupModMailThreadInDB(initialMessage, channel, userInServerId);
        });
    }

    @Transactional
    public void setupModMailThreadInDB(Message initialMessage, TextChannel channel, Long userInServerId) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(userInServerId);
        ModMailThread thread = createThreadObject(channel, aUserInAServer);
        if(initialMessage != null) {
            modMailMessageManagementService.addMessageToThread(thread, initialMessage, aUserInAServer, false, false);
        }
    }

    /**
     * Sends the message containing the pings to notify the staff members to handle the opened {@link ModMailThread}
     * @param aUserInAServer The {@link FullUserInServer} which opened the thread
     */
    @Transactional
    public CompletableFuture<Void> sendModMailNotification(FullUserInServer aUserInAServer) {
        List<ModMailRole> rolesToPing = modMailRoleManagementService.getRolesForServer(aUserInAServer.getAUserInAServer().getServerReference());
        ModMailNotificationModel modMailNotificationModel = ModMailNotificationModel
                .builder()
                .threadUser(aUserInAServer)
                .roles(rolesToPing)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_notification_message", modMailNotificationModel);
        List<CompletableFuture<Message>> modmailping = postTargetService.sendEmbedInPostTarget(messageToSend, ModMailPostTargets.MOD_MAIL_PING, aUserInAServer.getMember().getGuild().getIdLong());
        return CompletableFuture.allOf(modmailping.toArray(new CompletableFuture[0]));
    }

    /**
     * Creates the instance of the {@link ModMailThread} in the database.
     * @param channel The {@link TextChannel} in which the {@link ModMailThread} is being done
     * @param user The {@link AUserInAServer} which the thread is about
     * @return The created instance of {@link ModMailThread}
     */
    public ModMailThread createThreadObject(TextChannel channel, AUserInAServer user) {
        AChannel channel2 = channelManagementService.createChannel(channel.getIdLong(), AChannelType.TEXT, user.getServerReference());
        log.info("Creating mod mail thread in channel {} with db channel {}", channel.getIdLong(), channel2.getId());
        return modMailThreadManagementService.createModMailThread(user, channel2);
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
        // do nothing if we don't know the user
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
                            FullUserInServer fullUser = FullUserInServer.builder().member(memberInServer).aUserInAServer(chosenServer).build();
                            self.createModMailThreadForUser(fullUser, initialMessage, initialMessage.getChannel(), true, new ArrayList<>()).exceptionally(throwable -> {
                                log.error("Failed to setup thread correctly", throwable);
                                return null;
                            });
                        })
                        .build();
                menu.display(initialMessage.getChannel());
            } else if(availableGuilds.size() == 1) {
                // if exactly one server is available, open the thread directly
                AUserInAServer chosenServer = choices.get(availableGuilds.get(0).getReactionEmote());
                Member memberInServer = botService.getMemberInServer(chosenServer);
                FullUserInServer fullUser = FullUserInServer.builder().member(memberInServer).aUserInAServer(chosenServer).build();
                self.createModMailThreadForUser(fullUser, initialMessage, initialMessage.getChannel(), true, new ArrayList<>()).exceptionally(throwable -> {
                    log.error("Failed to setup thread correctly", throwable);
                    return null;
                });
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
     */
    private CompletableFuture<Void> sendModMailHeader(TextChannel channel, FullUserInServer aUserInAServer) {
        ModMailThread latestThread = modMailThreadManagementService.getLatestModMailThread(aUserInAServer.getAUserInAServer());
        List<ModMailThread> oldThreads = modMailThreadManagementService.getModMailThreadForUser(aUserInAServer.getAUserInAServer());
        ModMailThreaderHeader header = ModMailThreaderHeader
                .builder()
                .threadUser(aUserInAServer)
                .latestModMailThread(latestThread)
                .pastModMailThreadCount((long)oldThreads.size())
                .build();
        List<CompletableFuture<Message>> messages = channelService.sendEmbedTemplateInChannel("modmail_thread_header", header, channel);
        return CompletableFuture.allOf(messages.toArray(new CompletableFuture[0]));
    }

    @Override
    public CompletableFuture<Void> relayMessageToModMailThread(ModMailThread modMailThread, Message message, List<UndoActionInstance> undoActions) {
        Optional<TextChannel> textChannelFromServer = botService.getTextChannelFromServerOptional(modMailThread.getServer().getId(), modMailThread.getChannel().getId());
        if(textChannelFromServer.isPresent()) {
            TextChannel textChannel = textChannelFromServer.get();
            return self.sendUserReply(textChannel, modMailThread, message, modMailThread.getUser());
        } else {
            // in this case there was no text channel on the server associated with the mod mail thread
            // close the existing one, so the user can start a new one
            self.closeModMailThreadInDb(modMailThread.getId());
            return message.getChannel().sendMessage(templateService.renderTemplate("modmail_failed_to_forward_message", new Object())).submit().thenApply(message1 -> null);
        }
    }

    /**
     * This message takes a received {@link Message} from a user, renders it to a new message to send and sends it to
     * the appropriate {@link ModMailThread} channel, the returned promise only returns if the message was dealt with on the user
     * side.
     * @param textChannel The {@link TextChannel} in which the {@link ModMailThread} is being handled
     * @param modMailThread The {@link ModMailThread} to which the received {@link Message} is a reply to, can be null, if it is null, its the initial message
     * @param message The received message from the user
     * @return A {@link CompletableFuture} which resolves when the post processing of the message is completed (adding read notification, and storing messageIDs)
     */
    public CompletableFuture<Void> sendUserReply(TextChannel textChannel, ModMailThread modMailThread, Message message, AUserInAServer userInServer) {
        boolean modMailThreadExists = modMailThread != null;
        FullUserInServer fullUser = FullUserInServer
                .builder()
                .aUserInAServer(userInServer)
                .member(botService.getMemberInServer(userInServer))
                .build();

        List<FullUserInServer> subscribers = new ArrayList<>();
        if(modMailThreadExists) {
            List<ModMailThreadSubscriber> subscriberList = modMailSubscriberManagementService.getSubscribersForThread(modMailThread);
            subscriberList.forEach(modMailThreadSubscriber -> {
                FullUserInServer subscriber = FullUserInServer
                        .builder()
                        .aUserInAServer(modMailThreadSubscriber.getSubscriber())
                        .member(botService.getMemberInServer(modMailThreadSubscriber.getSubscriber()))
                        .build();
                subscribers.add(subscriber);
            });
        }
        ModMailUserReplyModel modMailUserReplyModel = ModMailUserReplyModel
                .builder()
                .modMailThread(modMailThread)
                .postedMessage(message)
                .threadUser(fullUser)
                .subscribers(subscribers)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_user_message", modMailUserReplyModel);
        List<CompletableFuture<Message>> completableFutures = channelService.sendMessageToSendToChannel(messageToSend, textChannel);
        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
                .thenCompose(aVoid -> messageService.addReactionToMessageWithFuture("readReaction", textChannel.getGuild().getIdLong(), message))
            .thenAccept(aVoid -> {
                if(modMailThreadExists) {
                    self.postProcessSendMessages(textChannel, completableFutures.get(0).join());
                }
            });

    }

    /**
     * This message handles the post processing of the messages received by the user. This includes: saving the messageIDs
     * in the database, updating the state of the {@link ModMailThread} and adding the read reaction to the user message
     * @param message The actual {@link Message} instance received from the user.
     */
    @Transactional
    public void postProcessSendMessages(TextChannel textChannel, Message message) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(textChannel.getIdLong());
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            modMailMessageManagementService.addMessageToThread(modMailThread, message, modMailThread.getUser(), false, false);
            // update the state of the thread
            modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.USER_REPLIED);
        } else {
            throw new ModMailThreadNotFoundException(textChannel.getIdLong());
        }
    }

    @Override
    public CompletableFuture<Void> relayMessageToDm(ModMailThread modMailThread, String text, Message message, boolean anonymous, MessageChannel feedBack, List<UndoActionInstance> undoActions) {
        Long modMailThreadId = modMailThread.getId();
        User userById = botService.getInstance().getUserById(modMailThread.getUser().getUserReference().getId());
        if(userById != null) {
            AUserInAServer moderator = userInServerManagementService.loadUser(message.getMember());
            Member userInGuild = botService.getMemberInServer(modMailThread.getUser());
            FullUserInServer fullThreadUser = FullUserInServer
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
            CompletableFuture<Message> future = messageService.sendEmbedToUserWithMessage(userById, "modmail_staff_message", modMailUserReplyModel);
            return future.thenAccept(sendMessage ->
                self.saveSendMessagesAndUpdateState(modMailThreadId, anonymous, moderator, sendMessage)
            );
        }
        throw new AbstractoRunTimeException("User in server not found.");
    }

    @Override
    public CompletableFuture<Void> closeModMailThread(ModMailThread modMailThread, String note, boolean notifyUser,  List<UndoActionInstance> undoActions) {
        AFeatureMode aFeatureMode = featureModeService.getFeatureMode(ModMailFeatures.MOD_MAIL, modMailThread.getServer());
        boolean loggingMode = aFeatureMode.getMode().equalsIgnoreCase(ModMailMode.LOGGING.getKey());
        return closeModMailThread(modMailThread, note, notifyUser, loggingMode, undoActions);
    }

    @Override
    public CompletableFuture<Void> closeModMailThread(ModMailThread modMailThread, String note, boolean notifyUser, boolean logThread, List<UndoActionInstance> undoActions) {
        Long modMailThreadId = modMailThread.getId();
        log.info("Starting closing procedure for thread {}", modMailThread.getId());
        List<ModMailMessage> modMailMessages = modMailThread.getMessages();
        if(logThread) {
            List<CompletableFuture<Message>> messages = modMailMessageService.loadModMailMessages(modMailMessages);
            CompletableFuture<Void> messagesFuture = FutureUtils.toSingleFutureGeneric(messages);

           return messagesFuture.handle((aVoid, throwable) ->
               self.logMessagesToModMailLog(note, notifyUser, modMailThreadId, undoActions, messages)
           ).toCompletableFuture().thenCompose(o -> o);

        } else {
            return self.afterSuccessfulLog(modMailThreadId, notifyUser, undoActions);
        }
    }

    /**
     * This method takes the actively loaded futures, calls the method responsible for logging the messages, and calls the method
     * after the logging has been done.
     * @param note The note which was provided when closing the {@link ModMailThread}
     * @param notifyUser Whether or not to notify the user
     * @param modMailThreadId The ID of the {@link ModMailThread} which is being closed
     * @param undoActions The list of {@link UndoActionInstance} to execute in case of exceptions
     * @param messages The list of loaded {@link Message} to log
     */
    @Transactional
    public CompletableFuture<Void> logMessagesToModMailLog(String note, Boolean notifyUser, Long modMailThreadId, List<UndoActionInstance> undoActions, List<CompletableFuture<Message>> messages) {
        try {
            CompletableFutureList<Message> list = self.logModMailThread(modMailThreadId, messages, note, undoActions);
            return list.getMainFuture().thenCompose(avoid -> {
                list.getFutures().forEach(messageCompletableFuture -> {
                    Message message = messageCompletableFuture.join();
                    undoActions.add(UndoActionInstance.getMessageDeleteAction(messageCompletableFuture.join().getChannel().getIdLong(), message.getIdLong()));
                });
                return self.afterSuccessfulLog(modMailThreadId, notifyUser, undoActions);
            });
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
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     */
    @Transactional
    public CompletableFuture<Void> afterSuccessfulLog(Long modMailThreadId, Boolean notifyUser,  List<UndoActionInstance> undoActions) {
        log.trace("Mod mail logging for thread {} has completed. Starting post logging activities.", modMailThreadId);
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            if(notifyUser) {
                ModMailThread modMailThread = modMailThreadOpt.get();
                User user = botService.getMemberInServer(modMailThread.getUser()).getUser();
                HashMap<String, String> closingMessage = new HashMap<>();
                String defaultValue = templateService.renderSimpleTemplate("modmail_closing_user_message_description");
                closingMessage.put("closingMessage", configService.getStringValue(MODMAIL_CLOSING_MESSAGE_TEXT, modMailThread.getServer().getId(), defaultValue));
                return messageService.sendEmbedToUser(user, "modmail_closing_user_message", closingMessage).thenAccept(message ->
                    self.deleteChannelAndClose(modMailThreadId, undoActions)
                );
            } else {
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
     */
    @Transactional
    public CompletableFuture<Void> deleteChannelAndClose(Long modMailThreadId, List<UndoActionInstance> undoActions) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            String failureMessage = "Failed to delete text channel containing mod mail thread {}";
            try {
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
     * @param note The note which was entered when closing the {@link ModMailThread}
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     * @return An instance of {@link CompletableFutureList}, which contains a main {@link CompletableFuture} which is resolved,
     * when all of the smaller {@link CompletableFuture} in it are resolved. We need this construct, because we need to access
     * the result values of the individual futures after they are done.
     */
    @Transactional
    public CompletableFutureList<Message> logModMailThread(Long modMailThreadId, List<CompletableFuture<Message>> messages, String note, List<UndoActionInstance> undoActions) {
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
            // TODO dont use this
            modMailThread.setClosed(Instant.now());
            ModMailClosingHeaderModel headerModel = ModMailClosingHeaderModel
                    .builder()
                    .closedThread(modMailThread)
                    .note(note)
                    .build();
            log.trace("Sending close header and individual mod mail messages to mod mail log target.");
            MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_close_header", headerModel);
            List<CompletableFuture<Message>> closeHeaderFutures = postTargetService.sendEmbedInPostTarget(messageToSend, ModMailPostTargets.MOD_MAIL_LOG, modMailThread.getServer().getId());
            // TODO in case the rendering fails, the already sent messages are not send
            completableFutures.addAll(closeHeaderFutures);
            completableFutures.addAll(self.sendMessagesToPostTarget(modMailThread, loggedMessages));
            return new CompletableFutureList<>(completableFutures);
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
            modMailThread.setClosed(Instant.now());
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
     * Evaluates the promises which were created when sending the messages to the private channel and stores the message IDs
     * and updates the state of the {@link ModMailThread}.
     * @param modMailThreadId The ID of the {@link ModMailThread} for which the messages were sent for
     * @param anonymous Whether or not the messages were send anonymous
     * @param moderator The original {@link AUserInAServer} which authored the messages
     * @throws ModMailThreadNotFoundException in case the {@link ModMailThread} is not found by the ID
     */
    @Transactional
    public void saveSendMessagesAndUpdateState(Long modMailThreadId, Boolean anonymous, AUserInAServer moderator, Message message) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            modMailMessageManagementService.addMessageToThread(modMailThread, message, moderator, anonymous, true);
            modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.MOD_REPLIED);
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }
    }
}
