package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.moderation.model.template.command.KickLogModel;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.CompletableFuture;

public interface KickService {
    String KICK_EFFECT_KEY = "kick";
    CompletableFuture<Void> kickMember(Member member, String reason, KickLogModel kickLogModel);
}
