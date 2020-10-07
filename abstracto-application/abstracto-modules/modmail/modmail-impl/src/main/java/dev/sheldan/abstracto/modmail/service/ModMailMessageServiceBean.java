package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.modmail.models.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import lombok.extern.slf4j.Slf4j;
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
    private BotService botService;

    @Override
    public List<CompletableFuture<Message>> loadModMailMessages(List<ModMailMessage> modMailMessages) {
        if(modMailMessages.isEmpty()) {
            return new ArrayList<>();
        }
        // all message must be from the same thread
        ModMailThread thread = modMailMessages.get(0).getThreadReference();
        log.trace("Loading {} mod mail messages from thread {} in server {}.", modMailMessages.size(), thread.getId(), thread.getServer().getId());
        List<ServerChannelMessage> messageIds = new ArrayList<>();
        modMailMessages.forEach(modMailMessage -> {
            ServerChannelMessage.ServerChannelMessageBuilder serverChannelMessageBuilder = ServerChannelMessage
                    .builder()
                    .messageId(modMailMessage.getMessageId());
            // if its not from a private chat, we need to set the server and channel ID in order to fetch the data
            if(Boolean.FALSE.equals(modMailMessage.getDmChannel())) {
                log.trace("Message {} was from DM.", modMailMessage.getMessageId());
                serverChannelMessageBuilder
                        .channelId(modMailMessage.getThreadReference().getChannel().getId())
                        .serverId(modMailMessage.getThreadReference().getServer().getId());
            }
            messageIds.add(serverChannelMessageBuilder.build());
        });
        List<CompletableFuture<Message>> messageFutures = new ArrayList<>();
        // add the place holder futures, which are then resolved one by one
        // because we cannot directly fetch the messages, in case they are in a private channel
        // the opening of a private channel is a rest operation it itself, so we need
        // to create the promises here already, else the list is empty for example
        modMailMessages.forEach(modMailMessage -> messageFutures.add(new CompletableFuture<>()));
        Optional<TextChannel> textChannelFromServer = botService.getTextChannelFromServerOptional(thread.getServer().getId(), thread.getChannel().getId());
        if(textChannelFromServer.isPresent()) {
            TextChannel modMailThread = textChannelFromServer.get();
            botService.getInstance().openPrivateChannelById(thread.getUser().getUserReference().getId()).queue(privateChannel -> {
                Iterator<CompletableFuture<Message>> iterator = messageFutures.iterator();
                messageIds.forEach(serverChannelMessage -> {
                    log.trace("Loading message {}.", serverChannelMessage.getMessageId());
                    // TODO fix out of order promises
                    // depending what the source of the message is, we need to fetch the message from the correct channel
                    if(serverChannelMessage.getChannelId() == null){
                        privateChannel.retrieveMessageById(serverChannelMessage.getMessageId()).queue(message -> iterator.next().complete(message), throwable -> {
                            log.info("Failed to load message in private channel with user {}", thread.getUser().getUserReference().getId());
                            iterator.next().complete(null);
                        });
                    } else {
                        modMailThread.retrieveMessageById(serverChannelMessage.getMessageId()).queue(message -> iterator.next().complete(message), throwable -> {
                            log.info("Failed to load message {} in thread {}", serverChannelMessage.getMessageId(), modMailThread.getIdLong());
                            iterator.next().complete(null);
                        });
                    }
                });
            });
        }
        return messageFutures;
    }
}
