package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.core.models.ServerChannelMessageUser;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.modmail.model.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.dto.LoadedModmailThreadMessage;
import dev.sheldan.abstracto.modmail.model.dto.LoadedModmailThreadMessageList;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ModMailMessageServiceBean implements ModMailMessageService {

    @Autowired
    private MemberService memberService;

    @Autowired
    private BotService botService;

    @Autowired
    private ChannelService channelService;

    @Override
    public LoadedModmailThreadMessageList loadModMailMessages(List<ModMailMessage> modMailMessages) {
        if(modMailMessages.isEmpty()) {
            return LoadedModmailThreadMessageList.builder().build();
        }
        // all message must be from the same thread
        ModMailThread thread = modMailMessages.get(0).getThreadReference();
        log.trace("Loading {} mod mail messages from thread {} in server {}.", modMailMessages.size(), thread.getId(), thread.getServer().getId());
        List<ServerChannelMessageUser> messageIds = new ArrayList<>();
        modMailMessages.forEach(modMailMessage -> {
            ServerChannelMessageUser.ServerChannelMessageUserBuilder serverChannelMessageBuilder = ServerChannelMessageUser
                    .builder()
                    .userId(modMailMessage.getAuthor().getUserReference().getId())
                    .serverId(thread.getServer().getId());
            // if its not from a private chat, we need to set channel ID in order to fetch the data
            if(Boolean.FALSE.equals(modMailMessage.getDmChannel())) {
                log.trace("Message {} was from DM.", modMailMessage.getMessageId());
                serverChannelMessageBuilder
                        .channelId(modMailMessage.getThreadReference().getChannel().getId());
                serverChannelMessageBuilder.messageId(modMailMessage.getCreatedMessageInChannel());
            } else {
                serverChannelMessageBuilder.messageId(modMailMessage.getCreatedMessageInDM());
            }
            messageIds.add(serverChannelMessageBuilder.build());
        });
        List<LoadedModmailThreadMessage> messageFutures = new ArrayList<>();
        // add the place holder futures, which are then resolved one by one
        // because we cannot directly fetch the messages, in case they are in a private channel
        // the opening of a private channel is a rest operation it itself, so we need
        // to create the promises here already, else the list is empty for example
        modMailMessages.forEach(modMailMessage -> messageFutures.add(getLoadedModmailThreadMessage()));
        Optional<TextChannel> textChannelFromServer = channelService.getTextChannelFromServerOptional(thread.getServer().getId(), thread.getChannel().getId());
        if(textChannelFromServer.isPresent()) {
            TextChannel modMailThread = textChannelFromServer.get();
            Long userId = thread.getUser().getUserReference().getId();
            botService.getInstance().openPrivateChannelById(userId).queue(privateChannel -> {
                Iterator<LoadedModmailThreadMessage> iterator = messageFutures.iterator();
                messageIds.forEach(serverChannelMessage -> {
                    log.trace("Loading message {}.", serverChannelMessage.getMessageId());
                    CompletableFuture<Message> messageFuture;
                    CompletableFuture<Member> memberFuture = memberService.getMemberInServerAsync(serverChannelMessage.getServerId(), serverChannelMessage.getUserId());
                    if(serverChannelMessage.getChannelId() == null){
                        messageFuture = channelService.retrieveMessageInChannel(privateChannel, serverChannelMessage.getMessageId());
                    } else {
                        messageFuture = channelService.retrieveMessageInChannel(modMailThread, serverChannelMessage.getMessageId());
                    }
                    CompletableFuture.allOf(messageFuture, memberFuture).whenComplete((aVoid, throwable) -> {
                        LoadedModmailThreadMessage next = iterator.next();
                        if(messageFuture.isCompletedExceptionally()) {
                            log.warn("Message {} from user {} in server {} failed to load.", serverChannelMessage.getMessageId(), serverChannelMessage.getUserId(), serverChannelMessage.getServerId());
                            messageFuture.exceptionally(throwable1 -> {
                                log.warn("Failed with:", throwable1);
                                return null;
                            });
                            next.getMessageFuture().complete(null);
                        } else {
                            next.getMessageFuture().complete(messageFuture.join());
                        }

                        if(memberFuture.isCompletedExceptionally()) {
                            next.getMemberFuture().complete(null);
                        } else {
                            next.getMemberFuture().complete(memberFuture.join());
                        }
                    });
                });
            });
        }
        return LoadedModmailThreadMessageList.builder().messageList(messageFutures).build();
    }

    public LoadedModmailThreadMessage getLoadedModmailThreadMessage() {
        return LoadedModmailThreadMessage.builder().memberFuture(new CompletableFuture<>()).messageFuture(new CompletableFuture<>()).build();
    }
}
