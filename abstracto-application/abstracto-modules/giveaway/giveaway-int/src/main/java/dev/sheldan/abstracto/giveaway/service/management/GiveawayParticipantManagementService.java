package dev.sheldan.abstracto.giveaway.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.giveaway.model.database.Giveaway;

public interface GiveawayParticipantManagementService {
    void addParticipant(Giveaway giveaway, AUserInAServer aUserInAServer);
    boolean userIsAlreadyParticipating(Giveaway giveaway, AUserInAServer aUserInAServer);
}
