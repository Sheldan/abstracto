package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.CompletableFuture;

public interface ReactionReportService {
    String REACTION_REPORT_EMOTE_KEY = "reactionReport";
    String REACTION_REPORT_COOLDOWN = "reactionReportCooldownSeconds";
    CompletableFuture<Void> createReactionReport(CachedMessage reportedMessage, ServerUser reporter, String context);
    CompletableFuture<Void> createReactionReport(Message message, ServerUser reporter, String context);
    boolean allowedToReport(ServerUser reporter);
}
