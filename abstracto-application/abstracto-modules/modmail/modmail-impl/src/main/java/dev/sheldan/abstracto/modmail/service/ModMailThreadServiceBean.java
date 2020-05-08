package dev.sheldan.abstracto.modmail.service;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import dev.sheldan.abstracto.core.exception.PostTargetException;
import dev.sheldan.abstracto.core.models.FullGuild;
import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.UndoActionInstance;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerService;
import dev.sheldan.abstracto.modmail.config.ModMailFeature;
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

    public static final String MODMAIL_CATEGORY = "modmailCategory";
    public static final String MODMAIL_LOG_POSTTARGET = "modmaillog";
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
    private ModMailThreadServiceBean self;

    private List<String> NUMBER_EMOJI = Arrays.asList("\u0031\u20e3", "\u0032\u20e3", "\u0033\u20e3",
            "\u0034\u20e3", "\u0035\u20e3", "\u0036\u20e3",
            "\u0037\u20e3", "\u0038\u20e3", "\u0039\u20e3",
            "\u0040\u20e3");


    @Override
    public void createModMailThreadForUser(FullUser aUserInAServer, MessageChannel feedBackChannel, Boolean userInitiated) {
        Long categoryId = configService.getLongValue(MODMAIL_CATEGORY, aUserInAServer.getAUserInAServer().getServerReference().getId());
        User user = aUserInAServer.getMember().getUser();
        CompletableFuture<TextChannel> textChannel = channelService.createTextChannel(user.getName() + user.getDiscriminator(), aUserInAServer.getAUserInAServer().getServerReference(), categoryId);

        textChannel.thenAccept(channel -> {
            List<UndoActionInstance> undoActions = new ArrayList<>();
            undoActions.add(UndoActionInstance.getChannelDeleteAction(channel.getIdLong(), aUserInAServer.getAUserInAServer().getServerReference().getId()));
            self.performModMailThreadSetup(aUserInAServer, channel, userInitiated, undoActions);
        }).exceptionally(throwable -> {
            log.error("Failed to create mod mail thread", throwable);
            sendModMailFailure("modmail_exception_failed_to_create_mod_mail_thread",  aUserInAServer.getAUserInAServer(), null, feedBackChannel, throwable);
            return null;
        });
    }

    @Transactional
    public void performModMailThreadSetup(FullUser aUserInAServer, TextChannel channel, Boolean userInitiated, List<UndoActionInstance> undoActions) {
        try {
            ModMailThread thread = createThreadObject(channel, aUserInAServer);
            sendModMailHeader(channel, aUserInAServer, undoActions);
            if(userInitiated) {
                sendModMailNotification(aUserInAServer, thread, undoActions);
            }
        } catch (Exception e) {
            log.error("Failed to perform mod mail thread setup.", e);
            undoActionService.performActions(undoActions);
        }
    }

    private void sendModMailNotification(FullUser aUserInAServer, ModMailThread thread, List<UndoActionInstance> undoActions) {
        List<ModMailRole> rolesToPing = modMailRoleManagementService.getRolesForServer(thread.getServer());
        ModMailNotificationModel modMailNotificationModel = ModMailNotificationModel
                .builder()
                .modMailThread(thread)
                .threadUser(aUserInAServer)
                .roles(rolesToPing)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_notification_message", modMailNotificationModel);
        List<CompletableFuture<Message>> modmailping = postTargetService.sendEmbedInPostTarget(messageToSend, "modmailping", thread.getServer().getId());
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
    public void createModMailPrompt(AUser user, MessageChannel channel) {
        List<AUserInAServer> knownServers = userInServerManagementService.getUserInAllServers(user.getId());
        if(knownServers.size() > 0) {
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
            if(availableGuilds.size() > 0) {
                ModMailServerChooserModel modMailServerChooserModel = ModMailServerChooserModel
                        .builder()
                        .commonGuilds(availableGuilds)
                        .build();
                String text = templateService.renderTemplate("modmail_modal_server_choice", modMailServerChooserModel);
                // todo dont instantiate directly
                EventWaiter waiter = new EventWaiter();
                botService.getInstance().addEventListener(waiter);
                ButtonMenu menu = new ButtonMenu.Builder()
                        .setChoices(choices.keySet().toArray(new String[0]))
                        .setEventWaiter(waiter)
                        .setDescription(text)
                        .setAction(reactionEmote -> {
                            AUserInAServer chosenServer = choices.get(reactionEmote.getEmoji());
                            Member memberInServer = botService.getMemberInServer(chosenServer);
                            FullUser fullUser = FullUser.builder().member(memberInServer).aUserInAServer(chosenServer).build();
                            self.createModMailThreadForUser(fullUser, channel, true);
                            botService.getInstance().removeEventListener(waiter);
                        })
                        .build();
                menu.display(channel);
            } else {
                channelService.sendTemplateInChannel("modmail_no_server_available", new Object(), channel);
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
        List<CompletableFuture<Message>> messages = channelService.sendTemplateInChannel("modmail_thread_header", header, channel);
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
        }
    }

    @Transactional
    public void sendUserReply(TextChannel textChannel, ModMailThread modMailThread, Message message) {
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
        List<Message> messages = new ArrayList<>();
        CompletableFuture.allOf(messages.toArray(new CompletableFuture[0])).thenAccept(aVoid -> {
            self.postProcessSendMessages(modMailThread, message, completableFutures, messages);
        });

    }

    @Transactional
    public void postProcessSendMessages(ModMailThread modMailThread, Message message, List<CompletableFuture<Message>> completableFutures, List<Message> messages) {
        completableFutures.forEach(messageCompletableFuture -> {
            try {
                Message messageToAdd = messageCompletableFuture.get();
                messages.add(messageToAdd);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error while executing future to retrieve reaction.", e);
            }
            self.saveMessageIds(messages, modMailThread, modMailThread.getUser(), false, false);
            modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.USER_REPLIED);
            messageService.addReactionToMessage("readReaction", modMailThread.getServer().getId(), message);
        });
    }

    @Override
    public void relayMessageToDm(ModMailThread modMailThread, String text, Message message, Boolean anonymous, MessageChannel feedBack) {
        User userById = botService.getInstance().getUserById(modMailThread.getUser().getUserReference().getId());
        if(userById != null) {
            userById.openPrivateChannel().queue(privateChannel -> {
                self.sendReply(modMailThread, text, message, privateChannel, anonymous, feedBack);
            }, throwable -> {
                log.warn("Failed to open private channel with user {}", userById.getIdLong());
            });
        }
    }

    @Transactional
    public void sendModMailFailure(String template, AUserInAServer aUserInAServer,  Long modMailTreadId, MessageChannel channel, Throwable throwable) {
        ModMailThread modMailThread = modMailThreadManagementService.getById(modMailTreadId);
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
            channelService.sendTemplateInChannel(template, modMailExceptionModel, channel);
        } catch (Exception e) {
            log.error("Failed to notify about mod mail exception.", e);
        }
    }

    @Override
    public synchronized void closeModMailThread(ModMailThread modMailThread, MessageChannel feedBack, String note, Boolean notifyUser) {
        List<ModMailMessage> modMailMessages = modMailThread.getMessages();
        List<CompletableFuture<Message>> messages = modMailMessageService.loadModMailMessages(modMailMessages);
        Long modMailThreadId = modMailThread.getId();
        for (int i = 0; i < messages.size(); i++) {
            CompletableFuture<Message> messageCompletableFuture = messages.get(i);
            Long messageId = modMailMessages.get(i).getMessageId();
            messageCompletableFuture.exceptionally(throwable -> {
                log.warn("Failed to load message {} in mod mail thread {}", messageId, modMailThreadId);
                return null;
            });
        }
        CompletableFuture.allOf(messages.toArray(new CompletableFuture[0])).whenComplete((avoid, throwable) -> {
            if(throwable != null) {
                log.warn("Failed to load some mod mail messages for mod mail thread {}. Still trying to post the ones we got.", modMailThreadId, throwable);
            }
            try {
                self.logModMailThread(modMailThreadId, messages, note).thenRun(() -> {
                    self.afterSuccessfulLog(modMailThreadId, feedBack, notifyUser);
                }).exceptionally(innerThrowable -> {
                    sendModMailFailure("modmail_exception_generic", modMailThread.getUser(), modMailThreadId, feedBack, innerThrowable);
                    log.error("Failed to log messages for mod mail thread {}.", modMailThreadId, innerThrowable);
                    return null;
                });
            } catch (PostTargetException po) {
                log.error("Failed to log mod mail messages", po);
                sendModMailFailure("modmail_exception_post_target_not_defined", modMailThread.getUser(), modMailThreadId, feedBack, po);
            } catch (Exception e) {
                log.error("Failed to log mod mail messages", e);
                sendModMailFailure("modmail_exception_generic", modMailThread.getUser(), modMailThreadId, feedBack, e);
            }
        });


    }

    @Transactional
    public void afterSuccessfulLog(Long modMailThreadId, MessageChannel feedBack, Boolean notifyUser) {
        ModMailThread modMailThread = modMailThreadManagementService.getById(modMailThreadId);
        User user = botService.getMemberInServer(modMailThread.getUser()).getUser();
        user.openPrivateChannel().queue(privateChannel ->  {
            try {
                List<CompletableFuture<Message>> messageFutures = new ArrayList<>();
                if(notifyUser){
                    messageFutures.addAll(channelService.sendTemplateInChannel("modmail_closing_user_message", new Object(), privateChannel));
                } else {
                    messageFutures.add(CompletableFuture.completedFuture(null));
                }
                CompletableFuture.allOf(messageFutures.toArray(new CompletableFuture[0])).whenComplete((result, throwable) -> {
                    if(throwable != null) {
                        log.warn("Failed to send closing message to user {} after closing mod mail thread {}", user.getIdLong(), modMailThread.getId(), throwable);
                    }
                    try {
                        channelService.deleteTextChannel(modMailThread.getChannel()).thenRun(() -> {
                            self.closeModMailThreadInDb(modMailThreadId);
                        }).exceptionally(throwable2 -> {
                            log.error("Failed to delete text channel containing mod mail thread {}", modMailThread.getId(), throwable2);
                            return null;
                        });
                    } catch (InsufficientPermissionException ex){
                        log.error("Failed to delete text channel containing mod mail thread {}", modMailThread.getId(), ex);
                        sendModMailFailure("modmail_exception_cannot_delete_channel", modMailThread.getUser(), modMailThreadId, feedBack, ex);
                    } catch (Exception ex) {
                        log.error("Failed to delete text channel containing mod mail thread {}", modMailThread.getId(), ex);
                    }
                });
            } catch (Exception e) {
                log.error("Failed to render closing user message", e);
                sendModMailFailure("modmail_exception_generic", modMailThread.getUser(), modMailThreadId, feedBack, e);
            }
        }, throwable -> log.error("Failed to load private channel with user {}", user.getIdLong(), throwable));
    }

    @Transactional
    public CompletableFuture<Void> logModMailThread(Long modMailThreadId, List<CompletableFuture<Message>> messages, String note) {
        ModMailThread modMailThread = modMailThreadManagementService.getById(modMailThreadId);
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
            }
        });
        List<CompletableFuture<Message>> completableFutures = new ArrayList<>();
        modMailThread.setClosed(Instant.now());
        ModMailClosingHeaderModel headerModel = ModMailClosingHeaderModel
                .builder()
                .closedThread(modMailThread)
                .note(note)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_close_header", headerModel);
        List<CompletableFuture<Message>> closeHeaderFutures = postTargetService.sendEmbedInPostTarget(messageToSend, MODMAIL_LOG_POSTTARGET, modMailThread.getServer().getId());
        completableFutures.addAll(closeHeaderFutures);
        completableFutures.addAll(self.sendMessagesToPostTarget(modMailThread, loggedMessages));
        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
    }

    @Transactional
    public void closeModMailThreadInDb(Long modMailThreadId) {
        ModMailThread modMailThread = modMailThreadManagementService.getById(modMailThreadId);
        log.info("Setting thread {} to closed.", modMailThread.getId());
        modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.CLOSED);
    }

    public List<CompletableFuture<Message>> sendMessagesToPostTarget(ModMailThread modMailThread, List<ModMailLoggedMessage> loadedMessages) {
        List<CompletableFuture<Message>> messageFutures = new ArrayList<>();
        loadedMessages.forEach(message -> {
            MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_close_logged_message", message);
            List<CompletableFuture<Message>> logFuture = postTargetService.sendEmbedInPostTarget(messageToSend, MODMAIL_LOG_POSTTARGET, modMailThread.getServer().getId());
            messageFutures.addAll(logFuture);
        });
        return messageFutures;
    }

    @Transactional
    public void sendReply(ModMailThread modMailThread, String text, Message message, PrivateChannel privateChannel, Boolean anonymous, MessageChannel feedBack) {
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
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenAccept(aVoid -> {
            List<Message> messages = new ArrayList<>();
            completableFutures.forEach(messageCompletableFuture -> {
                try {
                    Message messageToAdd = messageCompletableFuture.get();
                    messages.add(messageToAdd);
                } catch (InterruptedException | ExecutionException e) {
                    log.error("A future when sending the message to the user was interrupted.", e);
                }
            });
           self.saveMessageIds(messages, modMailThread, moderator, anonymous, true);
           modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.MOD_REPLIED);
        }).exceptionally(throwable -> {
            log.error("Failed to send message to user {}", modMailThread.getUser().getUserReference().getId());
            sendModMailFailure("modmail_exception_cannot_message_user", modMailThread.getUser(), modMailThread.getId(), feedBack, throwable);
            return null;
        });
    }

    @Transactional
    public void saveMessageIds(List<Message> messages, ModMailThread modMailThread, AUserInAServer author, Boolean anonymous, Boolean inDmChannel) {
        messages.forEach(message -> {
            modMailMessageManagementService.addMessageToThread(modMailThread, message, author, anonymous, inDmChannel);
        });
    }
}
