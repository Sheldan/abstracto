package dev.sheldan.abstracto.modmail.service;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import dev.sheldan.abstracto.core.exception.PostTargetNotFoundException;
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
import dev.sheldan.abstracto.modmail.exception.ModMailThreadNotFoundException;
import dev.sheldan.abstracto.modmail.models.database.*;
import dev.sheldan.abstracto.modmail.models.dto.ServerChoice;
import dev.sheldan.abstracto.modmail.models.template.*;
import dev.sheldan.abstracto.modmail.service.management.ModMailMessageManagementService;
import dev.sheldan.abstracto.modmail.service.management.ModMailRoleManagementService;
import dev.sheldan.abstracto.modmail.service.management.ModMailSubscriberManagementService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
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

    public static final String MODMAIL_CLOSING_MESSAGE_TEXT = "modMailClosingText";
    public static final String MODMAIL_CATEGORY = "modmailCategory";
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
    private ModMailThreadServiceBean self;

    private List<String> NUMBER_EMOJI = Arrays.asList("\u0031\u20e3", "\u0032\u20e3", "\u0033\u20e3",
            "\u0034\u20e3", "\u0035\u20e3", "\u0036\u20e3",
            "\u0037\u20e3", "\u0038\u20e3", "\u0039\u20e3",
            "\u0040\u20e3");


    @Override
    public void createModMailThreadForUser(FullUser aUserInAServer, Message initialMessage, MessageChannel feedBackChannel, boolean userInitiated) {
        Long categoryId = configService.getLongValue(MODMAIL_CATEGORY, aUserInAServer.getAUserInAServer().getServerReference().getId());
        User user = aUserInAServer.getMember().getUser();
        CompletableFuture<TextChannel> textChannel = channelService.createTextChannel(user.getName() + user.getDiscriminator(), aUserInAServer.getAUserInAServer().getServerReference(), categoryId);

        textChannel.thenAccept(channel -> {
            List<UndoActionInstance> undoActions = new ArrayList<>();
            undoActions.add(UndoActionInstance.getChannelDeleteAction(aUserInAServer.getAUserInAServer().getServerReference().getId(), channel.getIdLong()));
            self.performModMailThreadSetup(aUserInAServer, initialMessage, channel, userInitiated, undoActions);
        }).exceptionally(throwable -> {
            log.error("Failed to create mod mail thread", throwable);
            sendModMailFailure("modmail_exception_failed_to_create_mod_mail_thread",  aUserInAServer.getAUserInAServer(), null, feedBackChannel, throwable);
            return null;
        });
    }

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

    public ModMailThread createThreadObject(TextChannel channel, FullUser user) {
        AChannel channel2 = channelManagementService.createChannel(channel.getIdLong(), AChannelType.TEXT, user.getAUserInAServer().getServerReference());
        log.info("Creating mod mail thread in channel {} with db channel {}", channel.getIdLong(), channel2.getId());
        return modMailThreadManagementService.createModMailThread(user.getAUserInAServer(), channel2);
    }

    @Override
    public boolean hasOpenThread(AUserInAServer aUserInAServer) {
        return modMailThreadManagementService.getOpenModmailThreadForUser(aUserInAServer) != null;
    }

    @Override
    public boolean hasOpenThread(AUser user) {
        return modMailThreadManagementService.getOpenModmailThreadForUser(user) != null;
    }

    @Override
    public void setModMailCategoryTo(AServer server, Long categoryId) {
        configService.setLongValue(MODMAIL_CATEGORY, server.getId(), categoryId);
    }

    @Override
    public void createModMailPrompt(AUser user, Message initialMessage) {
        List<AUserInAServer> knownServers = userInServerManagementService.getUserInAllServers(user.getId());
        if(!knownServers.isEmpty()) {
            List<ServerChoice> availableGuilds = new ArrayList<>();
            HashMap<String, AUserInAServer> choices = new HashMap<>();
            for (int i = 0; i < knownServers.size(); i++) {
                AUserInAServer aUserInAServer = knownServers.get(i);
                if(featureFlagService.isFeatureEnabled(modMailFeature, aUserInAServer.getServerReference())) {
                    AServer serverReference = aUserInAServer.getServerReference();
                    FullGuild guild = FullGuild
                            .builder()
                            .guild(botService.getGuildByIdNullable(serverReference.getId()))
                            .server(serverReference)
                            .build();
                    String reactionEmote = NUMBER_EMOJI.get(i);
                    ServerChoice serverChoice = ServerChoice.builder().guild(guild).reactionEmote(reactionEmote).build();
                    choices.put(reactionEmote, aUserInAServer);
                    availableGuilds.add(serverChoice);
                }
            }
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
            } else {
                channelService.sendEmbedTemplateInChannel("modmail_no_server_available", new Object(), initialMessage.getChannel());
            }

        }
    }


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
                modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.USER_REPLIED);
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

    @Transactional
    public void sendModMailFailure(String template, AUserInAServer aUserInAServer,  Long modMailTreadId, MessageChannel channel, Throwable throwable) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailTreadId);
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
    public synchronized void closeModMailThread(ModMailThread modMailThread, MessageChannel feedBack, String note, boolean notifyUser) {
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

    private void logMessagesToModMailLog(MessageChannel feedBack, String note, Boolean notifyUser, Long modMailThreadId, List<UndoActionInstance> undoActions, List<CompletableFuture<Message>> messages, ModMailThread innerModMailThread) {
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
                sendModMailFailure(MODMAIL_EXCEPTION_GENERIC_TEMPLATE, innerModMailThread.getUser(), modMailThreadId, feedBack, innerThrowable);
                log.error("Failed to log messages for mod mail thread {}.", modMailThreadId, innerThrowable);
                return null;
            });
        } catch (PostTargetNotFoundException po) {
            log.error("Failed to log mod mail messages", po);
            sendModMailFailure("modmail_exception_post_target_not_defined", innerModMailThread.getUser(), modMailThreadId, feedBack, po);
        } catch (Exception e) {
            log.error("Failed to log mod mail messages", e);
            sendModMailFailure(MODMAIL_EXCEPTION_GENERIC_TEMPLATE, innerModMailThread.getUser(), modMailThreadId, feedBack, e);
        }
    }

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
                    sendModMailFailure(MODMAIL_EXCEPTION_GENERIC_TEMPLATE, modMailThread.getUser(), modMailThreadId, feedBack, e);
                }
            }, throwable -> {
                log.error("Failed to load private channel with user {}", user.getIdLong(), throwable);
                undoActionService.performActions(undoActions);
            });
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }
    }

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
                sendModMailFailure("modmail_exception_cannot_delete_channel", modMailThread.getUser(), modMailThreadId, feedBack, ex);
            } catch (Exception ex) {
                log.error(failureMessage, modMailThreadId, ex);
                undoActionService.performActions(undoActions);
            }
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }

    }

    @Transactional
    public CompletableFutureList<Message> logModMailThread(Long modMailThreadId, List<CompletableFuture<Message>> messages, String note) {
        log.info("Logging mod mail thread {}.", modMailThreadId);
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            List<ModMailLoggedMessage> loggedMessages = new ArrayList<>();
            messages.forEach(future -> {
                try {
                    if(!future.isCompletedExceptionally()) {
                        Message loadedMessage = future.get();
                        if(loadedMessage != null) {
                            ModMailMessage modmailMessage = modMailThread.getMessages()
                                    .stream()
                                    .filter(modMailMessage -> modMailMessage.getMessageId().equals(loadedMessage.getIdLong()))
                                    .findFirst().get();
                            ModMailLoggedMessage modMailLoggedMessage =
                                    ModMailLoggedMessage
                                            .builder()
                                            .message(loadedMessage)
                                            .modMailMessage(modmailMessage)
                                            .author(userInServerService.getFullUser(modmailMessage.getAuthor()))
                                            .build();
                            loggedMessages.add(modMailLoggedMessage);
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

    public List<CompletableFuture<Message>> sendMessagesToPostTarget(ModMailThread modMailThread, List<ModMailLoggedMessage> loadedMessages) {
        List<CompletableFuture<Message>> messageFutures = new ArrayList<>();
        loadedMessages.forEach(message -> {
            MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_close_logged_message", message);
            List<CompletableFuture<Message>> logFuture = postTargetService.sendEmbedInPostTarget(messageToSend, ModMailPostTargets.MOD_MAIL_LOG, modMailThread.getServer().getId());
            messageFutures.addAll(logFuture);
        });
        return messageFutures;
    }

    @Transactional
    public void sendReply(Long modMailThreadId, String text, Message message, PrivateChannel privateChannel, Boolean anonymous, MessageChannel feedBack) {
        Optional<ModMailThread> modMailThreadOpt = modMailThreadManagementService.getById(modMailThreadId);
        if(modMailThreadOpt.isPresent()) {
            ModMailThread modMailThread = modMailThreadOpt.get();
            AUserInAServer moderator = userInServerManagementService.loadUser(message.getMember());
            Member userInGuild = botService.getMemberInServer(modMailThread.getUser());
            Member moderatorMember = botService.getMemberInServer(moderator);
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
                modMailModeratorReplyModelBuilder.moderator(moderatorMember);
            }
            ModMailModeratorReplyModel modMailUserReplyModel = modMailModeratorReplyModelBuilder.build();
            MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_staff_message", modMailUserReplyModel);
            List<CompletableFuture<Message>> completableFutures = channelService.sendMessageToSendToChannel(messageToSend, privateChannel);
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenAccept(aVoid ->
                self.saveSendMessagesAndUpdateState(modMailThreadId, anonymous, moderator, completableFutures)
            ).exceptionally(throwable -> {
                log.error("Failed to send message to user {}", modMailThread.getUser().getUserReference().getId());
                sendModMailFailure("modmail_exception_cannot_message_user", modMailThread.getUser(), modMailThread.getId(), feedBack, throwable);
                return null;
            });
        } else {
            throw new ModMailThreadNotFoundException(modMailThreadId);
        }

    }

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

    public void saveMessageIds(List<Message> messages, ModMailThread modMailThread, AUserInAServer author, Boolean anonymous, Boolean inDmChannel) {
        messages.forEach(message ->
            modMailMessageManagementService.addMessageToThread(modMailThread, message, author, anonymous, inDmChannel)
        );
    }
}
