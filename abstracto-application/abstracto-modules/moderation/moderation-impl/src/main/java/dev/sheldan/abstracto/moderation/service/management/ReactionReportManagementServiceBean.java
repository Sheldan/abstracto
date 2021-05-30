package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.model.database.ReactionReport;
import dev.sheldan.abstracto.moderation.repository.ReactionReportRepository;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
public class ReactionReportManagementServiceBean implements ReactionReportManagementService {

    @Autowired
    private ReactionReportRepository repository;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public Optional<ReactionReport> findRecentReactionReportAboutUser(AUserInAServer aUserInAServer, Duration maxAge) {
        Instant maxCreation = Instant.now().minus(maxAge);
        return repository.findByReportedUserAndCreatedLessThan(aUserInAServer, maxCreation);
    }

    @Override
    public ReactionReport createReactionReport(CachedMessage reportedMessage, Message reportMessage) {
        AChannel reportChannel = channelManagementService.loadChannel(reportMessage.getTextChannel());
        AChannel reportedChannel = channelManagementService.loadChannel(reportedMessage.getChannelId());
        AUserInAServer reportedUser = userInServerManagementService.loadOrCreateUser(reportedMessage.getAuthorAsServerUser());
        ReactionReport report = ReactionReport
                        .builder()
                        .reportChannel(reportChannel)
                        .reportedChannel(reportedChannel)
                        .reportCount(1)
                        .reportedMessageId(reportedMessage.getMessageId())
                        .reportMessageId(reportMessage.getIdLong())
                        .reportedUser(reportedUser)
                        .server(reportedUser.getServerReference())
                        .build();
        return repository.save(report);
    }
}
