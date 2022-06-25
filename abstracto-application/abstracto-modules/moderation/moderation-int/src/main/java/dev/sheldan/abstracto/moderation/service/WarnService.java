package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.model.database.Warning;
import dev.sheldan.abstracto.moderation.model.template.command.WarnContext;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;


public interface WarnService {
    String WARN_EFFECT_KEY = "warn";
    String WARN_INFRACTION_TYPE = "warn";
    CompletableFuture<Void> notifyAndLogFullUserWarning(WarnContext context);
    CompletableFuture<Void> warnUserWithLog(WarnContext context);
    void decayWarning(Warning warning, Instant decayDate);
    CompletableFuture<Void> decayWarningsForServer(AServer server);
    CompletableFuture<Void> decayAllWarningsForServer(AServer server);
}
