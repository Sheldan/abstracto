package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.model.database.Mute;
import dev.sheldan.abstracto.moderation.repository.MuteRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class MuteManagementServiceBean implements MuteManagementService {

    @Autowired
    private MuteRepository muteRepository;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public Mute createMute(AUserInAServer mutedUser, AUserInAServer mutingUser, String reason, Instant unMuteDate, AServerAChannelMessage muteMessage, String triggerKey, Long muteId) {
        log.trace("Creating mute for user {} executed by user {} in server {}, user will be un-muted at {}",
                mutedUser.getUserReference().getId(), mutingUser.getUserReference().getId(), mutedUser.getServerReference().getId(), unMuteDate);
        ServerSpecificId id = new ServerSpecificId(muteMessage.getServer().getId(), muteId);
        Mute mute = Mute
                .builder()
                .mutedUser(mutedUser)
                .mutingUser(mutingUser)
                .muteTargetDate(unMuteDate)
                .server(mutedUser.getServerReference())
                .mutingChannel(muteMessage.getChannel())
                .messageId(muteMessage.getMessageId())
                .reason(reason)
                .triggerKey(triggerKey)
                .muteId(id)
                .muteEnded(false)
                .build();
        muteRepository.save(mute);
        return mute;
    }

    @Override
    public Optional<Mute> findMuteOptional(Long muteId, Long serverId) {
        return muteRepository.findByMuteId_IdAndMuteId_ServerId(muteId, serverId);
    }

    @Override
    public Mute findMute(Long muteId, Long serverId) {
        return findMuteOptional(muteId, serverId).orElseThrow(() -> new AbstractoRunTimeException("Mute not found."));
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

    @Override
    public boolean hasActiveMute(Member member) {
        return muteRepository.existsByMutedUserAndMuteEndedFalse(userInServerManagementService.loadOrCreateUser(member));
    }

    @Override
    public Mute getAMuteOf(AUserInAServer userInAServer) {
        return muteRepository.findTopByMutedUserAndMuteEndedFalse(userInAServer);
    }

    @Override
    public Mute getAMuteOf(Member member) {
        return getAMuteOf(userInServerManagementService.loadOrCreateUser(member));
    }

    @Override
    public List<Mute> getAllMutesOf(AUserInAServer aUserInAServer) {
        return muteRepository.findAllByMutedUserAndMuteEndedFalseOrderByMuteId_IdDesc(aUserInAServer);
    }


}
