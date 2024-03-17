package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.model.database.Warning;
import net.dv8tion.jda.api.entities.Guild;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;


public interface WarnService {
    String WARN_EFFECT_KEY = "warn";
    String WARN_INFRACTION_TYPE = "warn";
    CompletableFuture<Void> warnUserWithLog(Guild guild, ServerUser warnedUser, ServerUser warningUser, String reason, ServerChannelMessage serverChannelMessage);
    void decayWarning(Warning warning, Instant decayDate);
    CompletableFuture<Void> decayWarningsForServer(AServer server);
    CompletableFuture<Void> decayAllWarningsForServer(AServer server);
}
