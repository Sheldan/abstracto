package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.ReactionReport;
import net.dv8tion.jda.api.entities.Message;

import java.time.Duration;
import java.util.Optional;

public interface ReactionReportManagementService {
    Optional<ReactionReport> findRecentReactionReportAboutUser(AUserInAServer aUserInAServer, Duration part);
    ReactionReport createReactionReport(CachedMessage reportedMessage, Message reportMessage);
}
