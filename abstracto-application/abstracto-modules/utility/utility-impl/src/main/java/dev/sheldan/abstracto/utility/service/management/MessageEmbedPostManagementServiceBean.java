package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.command.service.UserService;
import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.AUserInAServer;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.converter.UserInServerConverter;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import dev.sheldan.abstracto.core.service.MessageService;
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
public class MessageEmbedPostManagementServiceBean  {

    @Autowired
    private EmbeddedMessageRepository embeddedMessageRepository;

    @Autowired
    private UserService userManagementService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserInServerConverter userInServerConverter;

    @Transactional
    public void createMessageEmbed(CachedMessage embeddedMessage, Message messageContainingEmbed, UserInServerDto cause) {
        AServer embeddedServer = AServer.builder().id(embeddedMessage.getServerId()).build();
        AChannel embeddedChannel = AChannel.builder().id(embeddedMessage.getChannelId()).build();
        AServer embeddingServer = AServer.builder().id(messageContainingEmbed.getGuild().getIdLong()).build();
        AChannel embeddingChannel = AChannel.builder().id(messageContainingEmbed.getTextChannel().getIdLong()).build();
        UserInServerDto authorDto = userManagementService.loadUser(embeddedMessage.getServerId(), embeddedMessage.getAuthorId());
        AUserInAServer author = userInServerConverter.fromDto(authorDto);
        AUserInAServer auserCause = userInServerConverter.fromDto(cause);
        EmbeddedMessage messageEmbedPost = EmbeddedMessage
                .builder()
                .embeddedMessageId(embeddedMessage.getMessageId())
                .embeddedChannel(embeddedChannel)
                .embeddedServer(embeddedServer)
                .embeddingServer(embeddingServer)
                .embeddingChannel(embeddingChannel)
                .embeddingMessageId(messageContainingEmbed.getIdLong())
                .embeddedUser(author)
                .embeddingUser(auserCause)
                .build();

        embeddedMessageRepository.save(messageEmbedPost);
    }

    public Optional<EmbeddedMessage> findEmbeddedPostByMessageId(Long messageId) {
        return Optional.ofNullable(embeddedMessageRepository.findByEmbeddingMessageId(messageId));
    }

    public void deleteEmbeddedMessage(EmbeddedMessage embeddedMessage) {
       embeddedMessageRepository.delete(embeddedMessage);
    }

    @Transactional
    public void deleteEmbeddedMessageTransactional(EmbeddedMessage embeddedMessage) {
        this.deleteEmbeddedMessage(embeddedMessage);
    }

}
