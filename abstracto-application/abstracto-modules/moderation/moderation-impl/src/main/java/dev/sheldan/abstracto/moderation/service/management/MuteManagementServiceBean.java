package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.models.database.Mute;
import dev.sheldan.abstracto.moderation.repository.MuteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class MuteManagementServiceBean implements MuteManagementService {

    @Autowired
    private MuteRepository muteRepository;

    @Override
    public Mute createMute(AUserInAServer aUserInAServer, AUserInAServer mutingUser, String reason, Instant unmuteDate, AServerAChannelMessage origin) {
        log.trace("Creating mute for user {} executed by user {} in server {}, user will be unmuted at {}",
                aUserInAServer.getUserReference().getId(), mutingUser.getUserReference().getId(), aUserInAServer.getServerReference().getId(), unmuteDate);
        Mute mute = Mute
                .builder()
                .muteDate(Instant.now())
                .mutedUser(aUserInAServer)
                .mutingUser(mutingUser)
                .muteTargetDate(unmuteDate)
                .mutingServer(aUserInAServer.getServerReference())
                .mutingChannel(origin.getChannel())
                .messageId(origin.getMessageId())
                .reason(reason)
                .muteEnded(false)
                .build();
        muteRepository.save(mute);
        return mute;
    }

    @Override
    public Mute findMute(Long muteId) {
        return muteRepository.getOne(muteId);
    }

    @Override
    public Mute saveMute(Mute mute) {
        muteRepository.save(mute);
        return mute;
    }

    @Override
    public boolean hasActiveMute(AUserInAServer userInAServer) {
        return muteRepository.existsByMutedUserAndMuteEndedFalse(userInAServer);
    }


}
