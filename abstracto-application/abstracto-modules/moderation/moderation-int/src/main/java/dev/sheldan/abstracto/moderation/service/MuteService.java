package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.Mute;
import dev.sheldan.abstracto.moderation.model.template.command.MuteContext;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public interface MuteService {
    String MUTE_EFFECT_KEY = "mute";
    CompletableFuture<Void> muteMember(Member memberToMute, Member userMuting, String reason, Instant unMuteDate, ServerChannelMessage message);
    CompletableFuture<Void> muteUserInServer(FullUserInServer userToMute, FullUserInServer userMuting, String reason, Instant unMuteDate, ServerChannelMessage message);
    CompletableFuture<Void> applyMuteRole(AUserInAServer aUserInAServer);
    CompletableFuture<Void> muteMemberWithLog(MuteContext context);
    String startUnMuteJobFor(Instant unMuteDate, Long muteId, Long serverId);
    void cancelUnMuteJob(Mute mute);
    CompletableFuture<Void> unMuteUser(AUserInAServer aUserInAServer);
    CompletableFuture<Void> endMute(Mute mute, Boolean sendNotification);
    CompletableFuture<Void> endMute(Long muteId, Long serverId);
    void completelyUnMuteUser(AUserInAServer aUserInAServer);
    void completelyUnMuteMember(Member member);
    CompletableFuture<Void> muteMemberWithoutContext(Member member);
}
