package dev.sheldan.abstracto.giveaway.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.giveaway.model.database.Giveaway;
import dev.sheldan.abstracto.giveaway.model.database.GiveawayParticipant;
import dev.sheldan.abstracto.giveaway.model.database.embed.GiveawayParticipationId;
import dev.sheldan.abstracto.giveaway.repository.GiveawayParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GiveawayParticipantManagementServiceBean implements GiveawayParticipantManagementService {

    @Autowired
    private GiveawayParticipantRepository repository;

    @Override
    public void addParticipant(Giveaway giveaway, AUserInAServer aUserInAServer) {
        GiveawayParticipationId id = new GiveawayParticipationId(aUserInAServer.getUserInServerId(), giveaway.getGiveawayId().getId(), giveaway.getServer().getId());
        GiveawayParticipant participant = GiveawayParticipant
                .builder()
                .id(id)
                .giveaway(giveaway)
                .participant(aUserInAServer)
                .won(false)
                .build();
        repository.save(participant);
    }

    @Override
    public boolean userIsAlreadyParticipating(Giveaway giveaway, AUserInAServer aUserInAServer) {
        return repository.existsById(new GiveawayParticipationId(aUserInAServer.getUserInServerId(), giveaway.getGiveawayId().getId(), giveaway.getServer().getId()));
    }
}
