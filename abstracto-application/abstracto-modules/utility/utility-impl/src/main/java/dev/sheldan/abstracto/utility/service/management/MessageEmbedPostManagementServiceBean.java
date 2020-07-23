package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.utility.exception.CrossServerEmbedException;
import dev.sheldan.abstracto.utility.models.database.EmbeddedMessage;
import dev.sheldan.abstracto.utility.repository.EmbeddedMessageRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    public void createMessageEmbed(CachedMessage embeddedMessage, Message messageContainingEmbed, AUserInAServer embeddingUser) {
        AServer embeddedServer = serverManagementService.loadOrCreate(embeddedMessage.getServerId());
        AServer embeddingServer = serverManagementService.loadOrCreate(messageContainingEmbed.getGuild().getIdLong());
        if(!embeddedServer.getId().equals(embeddingServer.getId())) {
            throw new CrossServerEmbedException(String.format("Message %s is not from server %s", embeddedMessage.getMessageUrl(), embeddingServer.getId()));
        }
        AChannel embeddingChannel = channelManagementService.loadChannel(messageContainingEmbed.getChannel().getIdLong());
        AChannel embeddedChannel = channelManagementService.loadChannel(embeddedMessage.getChannelId());
        AUserInAServer embeddedAuthor = userInServerManagementService.loadUser(embeddedMessage.getServerId(), embeddedMessage.getAuthorId());
        EmbeddedMessage messageEmbedPost = EmbeddedMessage
                .builder()
                .embeddedMessageId(embeddedMessage.getMessageId())
                .embeddedChannel(embeddedChannel)
                .embeddedServer(embeddedServer)
                .embeddingServer(embeddingServer)
                .embeddingChannel(embeddingChannel)
                .embeddingMessageId(messageContainingEmbed.getIdLong())
                .embeddedUser(embeddedAuthor)
                .embeddingUser(embeddingUser)
                .build();

        embeddedMessageRepository.save(messageEmbedPost);
    }

    @Override
    public Optional<EmbeddedMessage> findEmbeddedPostByMessageId(Long messageId) {
        return Optional.ofNullable(embeddedMessageRepository.findByEmbeddingMessageId(messageId));
    }

    @Override
    @Transactional
    public void deleteEmbeddedMessage(EmbeddedMessage embeddedMessage) {
       embeddedMessageRepository.delete(embeddedMessage);
    }

}
