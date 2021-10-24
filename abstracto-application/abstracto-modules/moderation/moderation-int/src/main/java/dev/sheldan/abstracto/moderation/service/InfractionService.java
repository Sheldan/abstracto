package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.Infraction;

import java.util.concurrent.CompletableFuture;

public interface InfractionService {
    void decayInfraction(Infraction infraction);
    Long getActiveInfractionPointsForUser(AUserInAServer aUserInAServer);
    CompletableFuture<Infraction> createInfractionWithNotification(AUserInAServer aUserInAServer, Long points);
    CompletableFuture<Void> createInfractionNotification(AUserInAServer aUserInAServer, Long points);
}
