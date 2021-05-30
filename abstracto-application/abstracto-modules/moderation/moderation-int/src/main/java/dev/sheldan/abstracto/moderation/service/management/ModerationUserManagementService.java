package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.ModerationUser;

import java.time.Instant;
import java.util.Optional;

public interface ModerationUserManagementService {
    ModerationUser createModerationUser(AUserInAServer aUserInAServer);
    ModerationUser createModerationUserWithReportTimeStamp(AUserInAServer aUserInAServer, Instant reportTime);
    Optional<ModerationUser> findModerationUser(AUserInAServer aUserInAServer);
}
