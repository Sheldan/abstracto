package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.models.database.AChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ChannelService {
    void sendTextInAChannel(String text, AChannel channel);
    List<CompletableFuture<Message>> sendMessageToEndInAChannel(MessageToSend messageToSend, AChannel channel);
    List<CompletableFuture<Message>> sendMessageToEndInTextChannel(MessageToSend messageToSend, TextChannel textChannel);
    Optional<TextChannel> getTextChannelInGuild(Long serverId, Long channelId);
}
