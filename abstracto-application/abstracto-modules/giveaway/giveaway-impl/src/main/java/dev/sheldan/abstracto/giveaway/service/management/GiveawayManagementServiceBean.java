package dev.sheldan.abstracto.giveaway.service.management;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.giveaway.model.database.Giveaway;
import dev.sheldan.abstracto.giveaway.repository.GiveawayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class GiveawayManagementServiceBean implements GiveawayManagementService {

    @Autowired
    private GiveawayRepository giveawayRepository;

    @Override
    public Giveaway createGiveaway(AUserInAServer creator, AUserInAServer benefactor, AChannel target,
                                   Instant targetDate, String title, String description, Integer winnerCount,
                                   Long messageId, String componentId, Long giveawayId) {
        Giveaway giveaway = Giveaway
                .builder()
                .giveawayId(new ServerSpecificId(creator.getServerReference().getId(), giveawayId))
                .creator(creator)
                .benefactor(benefactor)
                .messageId(messageId)
                .componentId(componentId)
                .server(creator.getServerReference())
                .winnerCount(winnerCount)
                .cancelled(false)
                .title(title)
                .giveawayChannel(target)
                .description(description)
                .targetDate(targetDate)
                .build();
        return giveawayRepository.save(giveaway);
    }

    @Override
    public Optional<Giveaway> loadGiveawayById(Long giveawayId, Long serverId) {
        return giveawayRepository.findById(new ServerSpecificId(serverId, giveawayId));
    }
}
