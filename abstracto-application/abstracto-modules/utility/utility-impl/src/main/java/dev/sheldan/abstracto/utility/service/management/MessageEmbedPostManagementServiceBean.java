package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
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

    @Override
    @Transactional
    public void createMessageEmbed(CachedMessage embeddedMessage, Message messageContainingEmbed, AUserInAServer cause) {
        AServer embeddedServer = AServer.builder().id(embeddedMessage.getServerId()).build();
        AChannel embeddedChannel = AChannel.builder().id(embeddedMessage.getChannelId()).build();
        AServer embeddingServer = AServer.builder().id(messageContainingEmbed.getGuild().getIdLong()).build();
        AChannel embeddingChannel = AChannel.builder().id(messageContainingEmbed.getTextChannel().getIdLong()).build();
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
                .embeddingUser(cause)
                .build();

        embeddedMessageRepository.save(messageEmbedPost);
    }

    @Override
    public Optional<EmbeddedMessage> findEmbeddedPostByMessageId(Long messageId) {
        return Optional.ofNullable(embeddedMessageRepository.findByEmbeddingMessageId(messageId));
    }

    @Override
    public void deleteEmbeddedMessage(EmbeddedMessage embeddedMessage) {
       embeddedMessageRepository.delete(embeddedMessage);
    }

    @Override
    @Transactional
    public void deleteEmbeddedMessageTransactional(EmbeddedMessage embeddedMessage) {
        this.deleteEmbeddedMessage(embeddedMessage);
    }

}
