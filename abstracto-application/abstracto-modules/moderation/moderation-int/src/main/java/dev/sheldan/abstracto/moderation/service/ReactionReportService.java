package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;

import java.util.concurrent.CompletableFuture;

public interface ReactionReportService {
    String REACTION_REPORT_EMOTE_KEY = "reactionReport";
    String REACTION_REPORT_COOLDOWN = "reactionReportCooldownSeconds";
    CompletableFuture<Void> createReactionReport(CachedMessage reportedMessage, ServerUser reporter);
}
