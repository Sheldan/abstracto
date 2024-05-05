package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.MuteResult;
import dev.sheldan.abstracto.moderation.model.database.Mute;
import dev.sheldan.abstracto.moderation.model.template.command.MuteLogModel;
import net.dv8tion.jda.api.entities.Guild;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public interface MuteService {
    String MUTE_EFFECT_KEY = "mute";
    String MUTE_INFRACTION_TYPE = "mute";
    String INFRACTION_PARAMETER_DURATION_KEY = "DURATION";
    CompletableFuture<MuteResult> muteUserInServer(Guild guild, ServerUser userBeingMuted, String reason, Duration duration);
    CompletableFuture<MuteResult> muteMemberWithLog(ServerUser userToMute, ServerUser mutingUser, String reason, Duration duration, Guild guild, ServerChannelMessage origin);
    CompletableFuture<MuteResult> muteMemberWithLog(ServerUser userToMute, ServerUser mutingUser, String reason, Duration duration, Guild guild, ServerChannelMessage origin, Instant oldTimeout);
    String startUnMuteJobFor(Instant unMuteDate, Long muteId, Long serverId);
    void cancelUnMuteJob(Mute mute);
    CompletableFuture<Void> unMuteUser(ServerUser userToUnMute, ServerUser memberUnMuting, Guild guild);
    CompletableFuture<Void> endMute(Mute mute, Guild guild);
    CompletableFuture<Void> endMute(Long muteId, Long serverId);
    CompletableFuture<Void> sendMuteLogMessage(MuteLogModel model, Long serverId);
    void completelyUnMuteUser(AUserInAServer aUserInAServer);
    void completelyUnMuteMember(ServerUser serverUser);
}
