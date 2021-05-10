package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.context.ServerContext;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.CompletableFuture;

public interface BanService {
    String BAN_EFFECT_KEY = "ban";
    CompletableFuture<Void> banMember(Member member, String reason, ServerContext banLog);
    CompletableFuture<Void> banUserViaId(Long guildId, Long userId, String reason, ServerContext banIdLog);
}
