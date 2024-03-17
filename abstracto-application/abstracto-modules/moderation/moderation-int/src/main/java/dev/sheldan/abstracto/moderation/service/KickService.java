package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.CompletableFuture;

public interface KickService {
    String KICK_EFFECT_KEY = "kick";
    String KICK_INFRACTION_TYPE = "kick";
    CompletableFuture<Void> kickMember(Member kickedMember, Member kickingMember, String reason);
    CompletableFuture<Void> kickMember(Guild guild, ServerUser kickedUser, String reason, ServerUser kickingUser);
}
