package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.repository.ModMailMessageRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ModMailMessageManagementServiceBean implements ModMailMessageManagementService {

    @Autowired
    private ModMailMessageRepository modMailMessageRepository;

    @Override
    public ModMailMessage addMessageToThread(ModMailThread modMailThread, Message createdMessageInDM, Message createdMessageInChannel, Message userPostedMessage, AUserInAServer author, Boolean anonymous, Boolean dmChannel) {
        Long dmId = createdMessageInDM != null ? createdMessageInDM.getIdLong() : null;
        Long channelMessageId = createdMessageInChannel != null ? createdMessageInChannel.getIdLong() : null;
        ModMailMessage modMailMessage = ModMailMessage
                .builder()
                .author(author)
                .messageId(userPostedMessage.getIdLong())
                .createdMessageInDM(dmId)
                .server(modMailThread.getServer())
                .createdMessageInChannel(channelMessageId)
                .dmChannel(dmChannel)
                .threadReference(modMailThread)
                .anonymous(anonymous)
                .build();
        log.info("Storing created message in DM {} with created message in channel {} caused by message {} to modmail thread {} of user {} in server {}.",
                dmId, channelMessageId, userPostedMessage.getId(), modMailThread.getId(), author.getUserReference().getId(), author.getServerReference().getId());

        modMailMessageRepository.save(modMailMessage);
        return modMailMessage;
    }

    @Override
    public List<ModMailMessage> getMessagesOfThread(ModMailThread modMailThread) {
        return modMailMessageRepository.findByThreadReference(modMailThread);
    }

    @Override
    public Optional<ModMailMessage> getByMessageIdOptional(Long messageId) {
        return modMailMessageRepository.findByMessageId(messageId);
    }

    @Override
    public void deleteMessageFromThread(ModMailMessage modMailMessage) {
        modMailMessageRepository.delete(modMailMessage);
    }
}
