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

@Component
@Slf4j
public class ModMailMessageManagementServiceBean implements ModMailMessageManagementService {

    @Autowired
    private ModMailMessageRepository modMailMessageRepository;

    @Override
    public ModMailMessage addMessageToThread(ModMailThread modMailThread, Message message, AUserInAServer author, Boolean anonymous, Boolean dmChannel) {
        ModMailMessage modMailMessage = ModMailMessage
                .builder()
                .author(author)
                .messageId(message.getIdLong())
                .dmChannel(dmChannel)
                .threadReference(modMailThread)
                .anonymous(anonymous)
                .build();
        log.info("Storing modmail thread message {} to modmail thread {} of user {} in server {}.",
                message.getId(), modMailThread.getId(), author.getUserReference().getId(), author.getServerReference().getId());

        modMailMessageRepository.save(modMailMessage);
        return modMailMessage;
    }

    @Override
    public List<ModMailMessage> getMessagesOfThread(ModMailThread modMailThread) {
        return modMailMessageRepository.findByThreadReference(modMailThread);
    }
}
