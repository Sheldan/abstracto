package dev.sheldan.abstracto.modmail.service;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import dev.sheldan.abstracto.core.models.FullGuild;
import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerService;
import dev.sheldan.abstracto.modmail.models.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.models.database.ModMailThreadState;
import dev.sheldan.abstracto.modmail.models.dto.ServerChoice;
import dev.sheldan.abstracto.modmail.models.template.*;
import dev.sheldan.abstracto.modmail.service.management.ModMailMessageManagementService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
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
    private ModMailThreadServiceBean self;

    private List<String> NUMBER_EMOJI = Arrays.asList("\u0031\u20e3", "\u0032\u20e3", "\u0033\u20e3",
            "\u0034\u20e3", "\u0035\u20e3", "\u0036\u20e3",
            "\u0037\u20e3", "\u0038\u20e3", "\u0039\u20e3",
            "\u0040\u20e3");


    @Override
    public void createModMailThreadForUser(FullUser aUserInAServer) {
        Long categoryId = configService.getLongValue(MODMAIL_CATEGORY, aUserInAServer.getAUserInAServer().getServerReference().getId());
        User user = aUserInAServer.getMember().getUser();
        CompletableFuture<TextChannel> textChannel = channelService.createTextChannel(user.getName() + user.getDiscriminator(), aUserInAServer.getAUserInAServer().getServerReference(), categoryId);

        textChannel.thenAccept(channel -> {
            self.createThreadObject(channel, aUserInAServer);
            self.sendWelcomeMessage(channel, aUserInAServer);
        });
    }

    @Transactional
    public void createThreadObject(TextChannel channel, FullUser user) {
        AChannel channel2 = channelManagementService.createChannel(channel.getIdLong(), AChannelType.TEXT, user.getAUserInAServer().getServerReference());
        log.info("Creating mod mail thread in channel {} with db channel {}", channel.getIdLong(), channel2.getId());
        modMailThreadManagementService.createModMailThread(user.getAUserInAServer(), channel2);
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
                        self.createModMailThreadForUser(fullUser);
                        botService.getInstance().removeEventListener(waiter);
                    })
                    .build();
            menu.display(channel);
        }
    }

    @Override
    public void sendWelcomeMessage(TextChannel channel, FullUser aUserInAServer) {
        String text = templateService.renderTemplate("modmail_welcome_message", new Object());
        channel.sendMessage(text).queue(message -> {}, throwable -> {
            log.warn("Failed to send welcome message to user {}", aUserInAServer.getAUserInAServer().getUserReference().getId());
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
        ModMailModeratorReplyModel modMailUserReplyModel = ModMailModeratorReplyModel
                .builder()
                .modMailThread(modMailThread)
                .postedMessage(message)
                .threadUser(fullUser)
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

    private void sendModMailFailure(String template, ModMailThread modMailThread, MessageChannel channel, Throwable throwable) {
        try {
            FullUser fullUser = FullUser
                    .builder()
                    .aUserInAServer(modMailThread.getUser())
                    .member(botService.getMemberInServer(modMailThread.getUser()))
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
    public void closeModMailThread(ModMailThread modMailThread, MessageChannel feedBack, String note) {
        List<ModMailMessage> modMailMessages = modMailThread.getMessages();

        List<CompletableFuture<Message>> messages = modMailMessageService.loadModMailMessages(modMailMessages);
        for (int i = 0; i < messages.size(); i++) {
            CompletableFuture<Message> messageCompletableFuture = messages.get(i);
            Long messageId = modMailMessages.get(i).getMessageId();
            messageCompletableFuture.exceptionally(throwable -> {
                log.warn("Failed to load message {} in mod mail thread {}", messageId, modMailThread.getId());
                return null;
            });
        }
        CompletableFuture.allOf(messages.toArray(new CompletableFuture[0])).thenAccept(aVoid -> {
            self.logModMailThread(modMailThread, messages, note, modMailMessages);
        }).exceptionally(throwable -> {
            log.warn("Failed to load some mod mail messages for mod mail thread {}. Still trying to post the ones we got.", modMailThread.getId(), throwable);
            self.logModMailThread(modMailThread, messages, note, modMailMessages);
            return null;
        });


    }

    @Transactional
    public void logModMailThread(ModMailThread modMailThread, List<CompletableFuture<Message>> messages, String note, List<ModMailMessage> modMailMessages) {
        List<ModMailLoggedMessage> loggedMessages = new ArrayList<>();
        messages.forEach(future -> {
            try {
                if(!future.isCompletedExceptionally()) {
                    Message loadedMessage = future.get();
                    if(loadedMessage != null) {
                        ModMailMessage modmailMessage = modMailMessages
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
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenAccept(aVoid -> {
            channelService.deleteTextChannel(modMailThread.getChannel()).thenAccept(o -> {
                self.closeModMailThreadInDb(modMailThread);
            });
        });
    }

    @Transactional
    public void closeModMailThreadInDb(ModMailThread modMailThread) {
        modMailThreadManagementService.setModMailThreadState(modMailThread, ModMailThreadState.CLOSED);
    }

    @Transactional
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
        FullUser moderatorUser = FullUser
                .builder()
                .aUserInAServer(moderator)
                .member(message.getMember())
                .build();
        FullUser fullThreadUser = FullUser
                .builder()
                .aUserInAServer(modMailThread.getUser())
                .member(userInGuild)
                .build();
        ModMailModeratorReplyModel modMailUserReplyModel = ModMailModeratorReplyModel
                .builder()
                .text(text)
                .modMailThread(modMailThread)
                .postedMessage(message)
                .threadUser(fullThreadUser)
                .moderator(moderatorUser)
                .build();
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
            sendModMailFailure("modmail_exception_cannot_message_user", modMailThread, feedBack, throwable);
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
