package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import net.dv8tion.jda.api.entities.Message;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface InfractionService {
    void decayInfraction(Infraction infraction);
    Long getActiveInfractionPointsForUser(AUserInAServer aUserInAServer);
    CompletableFuture<Infraction> createInfractionWithNotification(AUserInAServer target, Long points, String type, String description, AUserInAServer creator, Map<String, String> parameters, Message logMessage);
    CompletableFuture<Infraction> createInfractionWithNotification(AUserInAServer target, Long points, String type, String description, AUserInAServer creator, Map<String, String> parameters);
    CompletableFuture<Infraction> createInfractionWithNotification(AUserInAServer target, Long points, String type, String description, AUserInAServer creator, Message logMessage);
    CompletableFuture<Infraction> createInfractionWithNotification(AUserInAServer target, Long points, String type, String description, AUserInAServer creator);
    CompletableFuture<Void> createInfractionNotification(AUserInAServer aUserInAServer, Long points, String type, String description);
    CompletableFuture<Void> editInfraction(Long infractionId, String newReason, Long serverId);
}
