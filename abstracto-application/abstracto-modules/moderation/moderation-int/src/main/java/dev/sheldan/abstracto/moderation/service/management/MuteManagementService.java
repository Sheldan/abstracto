package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.models.database.Mute;

import java.time.Instant;

public interface MuteManagementService {
    Mute createMute(AUserInAServer aUserInAServer, AUserInAServer mutingUser, String reason, Instant unmuteDate, AServerAChannelMessage creation);
    Mute findMute(Long muteId);
    Mute saveMute(Mute mute);
    boolean hasActiveMute(AUserInAServer userInAServer);
}
