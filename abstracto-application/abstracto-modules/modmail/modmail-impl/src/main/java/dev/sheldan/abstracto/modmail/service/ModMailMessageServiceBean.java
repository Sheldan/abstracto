package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.ServerChannelMessageUser;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.UserService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.modmail.model.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.template.ModmailLoggingThreadMessages;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ModMailMessageServiceBean implements ModMailMessageService {

    @Autowired
    private MemberService memberService;

    @Autowired
    private BotService botService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserService userService;

    private static final Integer HISTORY_RETRIEVAL_LIMIT = 100;
    @Override
    public CompletableFuture<ModmailLoggingThreadMessages> loadModMailMessages(List<ModMailMessage> modMailMessages) {
        if(modMailMessages.isEmpty()) {
            return CompletableFuture.completedFuture(ModmailLoggingThreadMessages.builder().build());
        }
        CompletableFuture<ModmailLoggingThreadMessages> future = new CompletableFuture<>();
        // all message must be from the same thread
        ModMailThread thread = modMailMessages.get(0).getThreadReference();
        log.debug("Loading {} mod mail messages from thread {} in server {}.", modMailMessages.size(), thread.getId(), thread.getServer().getId());
        List<ServerChannelMessageUser> messageIds = new ArrayList<>();
        modMailMessages.forEach(modMailMessage -> {
            ServerChannelMessageUser.ServerChannelMessageUserBuilder serverChannelMessageBuilder = ServerChannelMessageUser
                    .builder()
                    .userId(modMailMessage.getAuthor().getUserReference().getId())
                    .serverId(thread.getServer().getId());
            // if its not from a private chat, we need to set channel ID in order to fetch the data
            // this is necessary, because we only log to the current modmail channel in a certain feature mode
            // but the DMs _always_ receive the messages from modmail thread.
            // the channelID is null, if it was a message from a modmail thread
            // which means, in order to retrieve the messages which were mod -> member
            // we need to select the elements in which channel is null
            if(Boolean.FALSE.equals(modMailMessage.getDmChannel())) {
                log.debug("Message {} was from DM.", modMailMessage.getMessageId());
                serverChannelMessageBuilder
                        .channelId(modMailMessage.getThreadReference().getChannel().getId());
                serverChannelMessageBuilder.messageId(modMailMessage.getCreatedMessageInChannel());
            } else {
                serverChannelMessageBuilder.messageId(modMailMessage.getCreatedMessageInDM());
            }
            messageIds.add(serverChannelMessageBuilder.build());
        });
        List<Long> messageIdsToLoad = messageIds
                .stream()
                .map(ServerChannelMessageUser::getMessageId)
                .collect(Collectors.toList());
        Optional<GuildMessageChannel> textChannelFromServer = channelService.getMessageChannelFromServerOptional(thread.getServer().getId(), thread.getChannel().getId());
        if(textChannelFromServer.isPresent()) {
            GuildMessageChannel modMailThread = textChannelFromServer.get();
            Long userId = thread.getUser().getUserReference().getId();
            botService.getInstance().openPrivateChannelById(userId).queue(privateChannel -> {
                Optional<ServerChannelMessageUser> latestThreadMessageOptional = messageIds
                        .stream()
                        .filter(serverChannelMessageUser -> serverChannelMessageUser.getChannelId() != null)
                        .max(Comparator.comparing(ServerChannelMessageUser::getMessageId));
                Optional<ServerChannelMessageUser> latestPrivateMessageOptional = messageIds
                        .stream()
                        .filter(serverChannelMessageUser -> serverChannelMessageUser.getChannelId() == null)
                        .max(Comparator.comparing(ServerChannelMessageUser::getMessageId));
                CompletableFuture<MessageHistory> threadHistoryFuture;
                if(latestThreadMessageOptional.isPresent()) {
                    ServerChannelMessageUser latestPrivateMessage = latestThreadMessageOptional.get();
                    threadHistoryFuture = modMailThread.getHistoryAround(latestPrivateMessage.getMessageId(), HISTORY_RETRIEVAL_LIMIT).submit();
                } else {
                    threadHistoryFuture = CompletableFuture.completedFuture(null);
                }
                CompletableFuture<MessageHistory> privateHistoryFuture;
                if(latestPrivateMessageOptional.isPresent()) {
                    ServerChannelMessageUser latestThreadMessage = latestPrivateMessageOptional.get();
                    privateHistoryFuture = privateChannel.getHistoryAround(latestThreadMessage.getMessageId(), HISTORY_RETRIEVAL_LIMIT).submit();
                } else {
                    privateHistoryFuture = CompletableFuture.completedFuture(null);
                }
                List<Message> loadedMessages = new ArrayList<>();
                CompletableFuture.allOf(threadHistoryFuture, privateHistoryFuture)
                        .thenCompose(unused -> loadMoreMessages(messageIdsToLoad.size(), messageIdsToLoad, privateHistoryFuture.join(), modMailThread, threadHistoryFuture.join(), privateChannel, loadedMessages, 0))
                .thenAccept(unused -> {
                    Set<Long> userIds = messageIds
                            .stream()
                            .map(ServerChannelMessageUser::getUserId)
                            .collect(Collectors.toSet());
                    CompletableFutureList<User> userFuture = userService.retrieveUsers(new ArrayList<>(userIds));
                    userFuture.getMainFuture().thenAccept(unused1 -> {
                        ModmailLoggingThreadMessages result = ModmailLoggingThreadMessages
                                .builder()
                                .messages(loadedMessages)
                                .authors(userFuture.getObjects())
                                .build();
                        future.complete(result);
                    });
                });
            });
        } else {
            future.completeExceptionally(new AbstractoRunTimeException("Channel for modmail thread not found. How did we get here?"));
        }
        return future;
    }

    public CompletableFuture<Void> loadMoreMessages(Integer totalMessageCount, List<Long> messagesToLoad,
                                                    MessageHistory privateMessageHistory, GuildMessageChannel thread,
                                                    MessageHistory threadMessageHistory, PrivateChannel dmChannel, List<Message> loadedMessages, Integer counter) {
        // TODO maybe find a better mechanism for this...  one which does not lead to infinite loops, but also doesnt miss out on history
        if(counter.equals(totalMessageCount * 2)) {
            log.warn("We encountered the maximum of {} iterations when loading modmail history - aborting.", messagesToLoad.size());
            return CompletableFuture.completedFuture(null);
        }
        Map<Long, Message> threadMessagesInStep = mapHistoryToMessageIds(threadMessageHistory);
        Map<Long, Message> privateMessagesInStep = mapHistoryToMessageIds(privateMessageHistory);
        List<Long> messagesLoadedThisStep = new ArrayList<>();
        messagesToLoad.forEach(messageId -> {
            if(threadMessagesInStep.containsKey(messageId)) {
                loadedMessages.add(threadMessagesInStep.get(messageId));
                messagesLoadedThisStep.add(messageId);
            } else if(privateMessagesInStep.containsKey(messageId)){
                loadedMessages.add(privateMessagesInStep.get(messageId));
                messagesLoadedThisStep.add(messageId);
            }
        });
        messagesToLoad.removeAll(messagesLoadedThisStep);
        if(messagesToLoad.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        } else {
            final CompletableFuture<MessageHistory> threadHistoryAction;
            if(doesHistoryContainValues(threadMessageHistory)) {
                Optional<Message> minThreadMessage = getOldestMessage(threadMessageHistory.getRetrievedHistory());
                if(minThreadMessage.isPresent()) {
                    threadHistoryAction = thread.getHistoryBefore(minThreadMessage.get(), HISTORY_RETRIEVAL_LIMIT).submit();
                } else {
                    threadHistoryAction = CompletableFuture.completedFuture(null);
                }
            } else {
                threadHistoryAction = CompletableFuture.completedFuture(null);
            }

            final CompletableFuture<MessageHistory> privateHistoryAction;
            if(doesHistoryContainValues(privateMessageHistory)) {
                Optional<Message> minDmMessage = getOldestMessage(privateMessageHistory.getRetrievedHistory());
                if(minDmMessage.isPresent()) {
                    privateHistoryAction = dmChannel.getHistoryBefore(minDmMessage.get(), HISTORY_RETRIEVAL_LIMIT).submit();
                } else {
                    privateHistoryAction = CompletableFuture.completedFuture(null);
                }
            } else {
                privateHistoryAction = CompletableFuture.completedFuture(null);
            }
            return CompletableFuture.allOf(threadHistoryAction, privateHistoryAction)
                    .thenCompose(lists -> loadMoreMessages(totalMessageCount, messagesToLoad, threadHistoryAction.join(), thread, privateHistoryAction.join(), dmChannel, loadedMessages, counter + 1));
        }
    }

    private Map<Long, Message> mapHistoryToMessageIds(MessageHistory threadMessageHistory) {
        if(!doesHistoryContainValues(threadMessageHistory)) {
            return new HashMap<>();
        }
        return threadMessageHistory
                .getRetrievedHistory()
                .stream()
                .collect(Collectors.toMap(ISnowflake::getIdLong, Function.identity()));
    }

    private boolean doesHistoryContainValues(MessageHistory threadMessageHistory) {
        return threadMessageHistory != null && !threadMessageHistory.isEmpty();
    }

    private Optional<Message> getOldestMessage(List<Message> messages) {
        return messages.stream().min(Comparator.comparing(ISnowflake::getTimeCreated));
    }

}
