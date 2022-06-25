package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.Mute;
import dev.sheldan.abstracto.moderation.model.template.command.MuteContext;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public interface MuteService {
    String MUTE_EFFECT_KEY = "mute";
    String MUTE_INFRACTION_TYPE = "mute";
    String INFRACTION_PARAMETER_DURATION_KEY = "DURATION";
    CompletableFuture<Void> muteMember(Member memberToMute, String reason, Instant unMuteDate, Long channelId);
    CompletableFuture<Void> muteUserInServer(FullUserInServer userToMute, String reason, Instant unMuteDate, Long channelId);
    CompletableFuture<Void> muteMemberWithLog(MuteContext context);
    String startUnMuteJobFor(Instant unMuteDate, Long muteId, Long serverId);
    void cancelUnMuteJob(Mute mute);
    CompletableFuture<Void> unMuteUser(AUserInAServer userToUnmute, Member memberUnMuting);
    CompletableFuture<Void> endMute(Mute mute);
    CompletableFuture<Void> endMute(Long muteId, Long serverId);
    void completelyUnMuteUser(AUserInAServer aUserInAServer);
    void completelyUnMuteMember(Member member);
}
