package dev.sheldan.abstracto.entertainment.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.entertainment.model.database.PressF;

import java.time.Instant;
import java.util.Optional;

public interface PressFManagementService {
    PressF createPressF(String text, Instant targetDate, AUserInAServer creator, AChannel channel, Long messageId);
    Optional<PressF> getPressFById(Long pressFId);
}
