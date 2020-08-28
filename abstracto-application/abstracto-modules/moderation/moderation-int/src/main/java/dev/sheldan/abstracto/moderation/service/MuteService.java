package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.models.database.Mute;
import dev.sheldan.abstracto.moderation.models.template.commands.MuteLog;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.time.Instant;

public interface MuteService {
    Mute muteMember(Member memberToMute, Member userMuting, String reason, Instant unMuteDate, Message message);
    Mute muteAUserInAServer(AUserInAServer member, AUserInAServer userMuting, String reason, Instant unMuteDate, Message message);
    Mute muteUser(FullUserInServer userToMute, FullUserInServer userMuting, String reason, Instant unMuteDate, Message message);
    void applyMuteRole(AUserInAServer aUserInAServer);
    void muteMemberWithLog(Member memberToMute, Member memberMuting, String reason, Instant unMuteDate, MuteLog log, Message message);
    String startUnMuteJobFor(Instant unMuteDate, Mute mute);
    void cancelUnMuteJob(Mute mute);
    void unMuteUser(Mute mute);
    void endMute(Long muteId);
    void completelyUnMuteUser(AUserInAServer aUserInAServer);
    void completelyUnMuteMember(Member member);
}
