package dev.sheldan.abstracto.linkembed.service.management;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.linkembed.exception.CrossServerEmbedException;
import dev.sheldan.abstracto.linkembed.model.database.EmbeddedMessage;
import dev.sheldan.abstracto.linkembed.repository.EmbeddedMessageRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class MessageEmbedPostManagementServiceBean implements MessageEmbedPostManagementService {

    @Autowired
    private EmbeddedMessageRepository embeddedMessageRepository;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    @Transactional
    public void createMessageEmbed(CachedMessage embeddedMessage, Message messageContainingEmbed, AUserInAServer embeddingUser, String deletionComponentId) {
        AServer embeddedServer = serverManagementService.loadOrCreate(embeddedMessage.getServerId());
        AServer embeddingServer = serverManagementService.loadOrCreate(messageContainingEmbed.getGuild().getIdLong());
        if(!embeddedMessage.getServerId().equals(messageContainingEmbed.getGuild().getIdLong())) {
            throw new CrossServerEmbedException(String.format("Message %s is not from server %s", embeddedMessage.getMessageUrl(), embeddingServer.getId()));
        }
        AChannel embeddingChannel = channelManagementService.loadChannel(messageContainingEmbed.getChannel().getIdLong());
        AChannel embeddedChannel = channelManagementService.loadChannel(embeddedMessage.getChannelId());
        AUserInAServer embeddedAuthor = userInServerManagementService.loadOrCreateUser(embeddedMessage.getServerId(), embeddedMessage.getAuthor().getAuthorId());
        EmbeddedMessage messageEmbedPost = EmbeddedMessage
                .builder()
                .embeddedMessageId(embeddedMessage.getMessageId())
                .embeddedChannel(embeddedChannel)
                .embeddedServer(embeddedServer)
                .deletionComponentId(deletionComponentId)
                .embeddingServer(embeddingServer)
                .embeddingChannel(embeddingChannel)
                .embeddingMessageId(messageContainingEmbed.getIdLong())
                .embeddedUser(embeddedAuthor)
                .embeddingUser(embeddingUser)
                .build();

        log.info("Saving embedded post: message {} by user {} in channel {} in server {} embedded message {} by user {} in channel {} in server {}.",
                messageContainingEmbed.getIdLong(), messageContainingEmbed.getAuthor().getIdLong(), embeddingChannel.getId(), messageContainingEmbed.getChannel().getIdLong(),
                embeddedMessage.getMessageId(), embeddedMessage.getAuthor().getAuthorId(), embeddedMessage.getChannelId(), embeddedMessage.getServerId());

        embeddedMessageRepository.save(messageEmbedPost);
    }

    @Override
    public Optional<EmbeddedMessage> findEmbeddedPostByMessageId(Long messageId) {
        return Optional.ofNullable(embeddedMessageRepository.findByEmbeddingMessageId(messageId));
    }

    @Override
    @Transactional
    public void deleteEmbeddedMessage(EmbeddedMessage embeddedMessage) {
        log.info("Deleting embedded message {}.", embeddedMessage.getEmbeddingMessageId());
       embeddedMessageRepository.delete(embeddedMessage);
    }

    @Override
    public List<EmbeddedMessage> getEmbeddedMessagesOlderThan(Instant date) {
        return embeddedMessageRepository.findByCreatedLessThan(date);
    }

    @Override
    public void deleteEmbeddedMessagesViaId(List<Long> embeddingMessageId) {
        log.info("Deleting {} embedded messages from db.", embeddingMessageId.size());
        embeddedMessageRepository.deleteByEmbeddingMessageIdIn(embeddingMessageId);
    }

}
