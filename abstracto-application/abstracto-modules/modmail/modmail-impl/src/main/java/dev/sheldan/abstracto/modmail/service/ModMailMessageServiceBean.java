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
        ModMailThread thread = modMailMessages.get(0).getThreadReference();
        List<ServerChannelMessage> messageIds = new ArrayList<>();
        modMailMessages.forEach(modMailMessage -> {
            ServerChannelMessage.ServerChannelMessageBuilder serverChannelMessageBuilder = ServerChannelMessage
                    .builder()
                    .messageId(modMailMessage.getMessageId());
            if(Boolean.FALSE.equals(modMailMessage.getDmChannel())) {
                serverChannelMessageBuilder
                        .channelId(modMailMessage.getThreadReference().getChannel().getId())
                        .serverId(modMailMessage.getThreadReference().getServer().getId());
            }
            messageIds.add(serverChannelMessageBuilder.build());
        });
        List<CompletableFuture<Message>> messageFutures = new ArrayList<>();
        modMailMessages.forEach(modMailMessage -> messageFutures.add(new CompletableFuture<>()));
        Optional<TextChannel> textChannelFromServer = botService.getTextChannelFromServer(thread.getServer().getId(), thread.getChannel().getId());
        if(textChannelFromServer.isPresent()) {
            TextChannel modMailThread = textChannelFromServer.get();
            botService.getInstance().openPrivateChannelById(thread.getUser().getUserReference().getId()).queue(privateChannel -> {
                Iterator<CompletableFuture<Message>> iterator = messageFutures.iterator();
                messageIds.forEach(serverChannelMessage -> {
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
