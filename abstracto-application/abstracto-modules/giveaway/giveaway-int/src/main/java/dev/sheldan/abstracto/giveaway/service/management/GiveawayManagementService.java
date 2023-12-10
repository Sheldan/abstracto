package dev.sheldan.abstracto.giveaway.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.giveaway.model.database.Giveaway;

import java.time.Instant;
import java.util.Optional;

public interface GiveawayManagementService {
    Giveaway createGiveaway(AUserInAServer creator, AUserInAServer benefactor, AChannel target,
                            Instant targetDate, String title, String description, Integer winnerCount, Long messageId,
                            String componentId, Long giveawayId);
    Optional<Giveaway> loadGiveawayById(Long giveawayId, Long serverId);
}
