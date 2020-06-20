package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.models.database.Mute;
import dev.sheldan.abstracto.moderation.models.template.commands.MuteLog;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.time.Instant;

public interface MuteService {
    Mute muteMember(Member memberToMute, Member userMuting, String reason, Instant unmuteDate, Message message);
    Mute muteAUserInAServer(AUserInAServer member, AUserInAServer userMuting, String reason, Instant unmuteDate, Message message);
    Mute muteUser(FullUser userToMute, FullUser userMuting, String reason, Instant unmuteDate, Message message);
    void applyMuteRole(AUserInAServer aUserInAServer);
    void muteMemberWithLog(Member memberToMute, Member memberMuting, String reason, Instant unmuteDate, MuteLog log, Message message);
    String startUnmuteJobFor(Instant unmuteDate, Mute mute);
    void cancelUnmuteJob(Mute mute);
    void unmuteUser(Mute mute);
    void endMute(Long muteId);
    void completelyUnMuteUser(AUserInAServer aUserInAServer);
    void completelyUnMuteMember(Member member);
}
