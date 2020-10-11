package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.models.database.Mute;
import dev.sheldan.abstracto.moderation.models.template.commands.MuteContext;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public interface MuteService {
    CompletableFuture<Void> muteMember(Member memberToMute, Member userMuting, String reason, Instant unMuteDate, ServerChannelMessage message);
    CompletableFuture<Void> muteUserInServer(FullUserInServer userToMute, FullUserInServer userMuting, String reason, Instant unMuteDate, ServerChannelMessage message);
    CompletableFuture<Void> applyMuteRole(AUserInAServer aUserInAServer);
    CompletableFuture<Void> muteMemberWithLog(MuteContext context);
    String startUnMuteJobFor(Instant unMuteDate, Long muteId, Long serverId);
    void cancelUnMuteJob(Mute mute);
    CompletableFuture<Void> unMuteUser(AUserInAServer aUserInAServer);
    CompletableFuture<Void> endMute(Mute mute);
    CompletableFuture<Void> endMute(Long muteId, Long serverId);
    void completelyUnMuteUser(AUserInAServer aUserInAServer);
    void completelyUnMuteMember(Member member);
}
