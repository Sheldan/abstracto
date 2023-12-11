package dev.sheldan.abstracto.entertainment.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.entertainment.model.database.PressF;
import dev.sheldan.abstracto.entertainment.repository.PressFRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class PressFManagementServiceBean implements PressFManagementService {

    @Autowired
    private PressFRepository pressFRepository;
    @Override
    public PressF createPressF(String text, Instant targetDate, AUserInAServer creator, AChannel channel, Long messageId) {
        PressF pressF = PressF
                .builder()
                .server(creator.getServerReference())
                .creator(creator)
                .messageId(messageId)
                .pressFChannel(channel)
                .text(text)
                .targetDate(targetDate)
                .build();
        return pressFRepository.save(pressF);
    }

    @Override
    public Optional<PressF> getPressFById(Long pressFId) {
        return pressFRepository.findById(pressFId);
    }
}
