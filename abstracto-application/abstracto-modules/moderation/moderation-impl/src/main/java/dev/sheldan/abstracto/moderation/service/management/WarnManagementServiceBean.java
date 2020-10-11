package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.repository.WarnRepository;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class WarnManagementServiceBean implements WarnManagementService {

    @Autowired
    private WarnRepository warnRepository;

    @Override
    public Warning createWarning(AUserInAServer warnedAUser, AUserInAServer warningAUser, String reason, Long warnId) {
        log.info("Creating warning with id {} for user {} in server {} cast by user {}.",
                warnId, warnedAUser.getUserReference().getId(), warningAUser.getServerReference().getId(), warningAUser.getUserReference().getId());
        ServerSpecificId warningId = new ServerSpecificId(warningAUser.getServerReference().getId(), warnId);
        Warning warning = Warning.builder()
                .reason(reason)
                .warnedUser(warnedAUser)
                .warningUser(warningAUser)
                .warnDate(Instant.now())
                .server(warningAUser.getServerReference())
                .warnId(warningId)
                .decayed(false)
                .build();
        warnRepository.save(warning);
        return warning;
    }

    @Override
    public List<Warning> getActiveWarningsInServerOlderThan(AServer server, Instant date) {
        return warnRepository.findAllByWarnedUser_ServerReferenceAndDecayedFalseAndWarnDateLessThan(server, date);
    }

    @Override
    public Long getTotalWarnsForUser(AUserInAServer aUserInAServer) {
        return warnRepository.countByWarnedUser(aUserInAServer);
    }

    @Override
    public List<Warning> getAllWarnsForUser(AUserInAServer aUserInAServer) {
        return warnRepository.findByWarnedUser(aUserInAServer);
    }

    @Override
    public List<Warning> getAllWarningsOfServer(AServer server) {
        return warnRepository.findAllByWarnedUser_ServerReference(server);
    }

    @Override
    public Long getActiveWarnsForUser(AUserInAServer aUserInAServer) {
        return warnRepository.countByWarnedUserAndDecayedFalse(aUserInAServer);
    }

    @Override
    public Optional<Warning> findByIdOptional(Long id, Long serverId) {
        return warnRepository.findByWarnId_IdAndWarnId_ServerId(id, serverId);
    }

    @Override
    public Warning findById(Long id, Long serverId) {
        return findByIdOptional(id, serverId).orElseThrow(() -> new AbstractoRunTimeException("Warning not found."));
    }

    @Override
    public void deleteWarning(Warning  warning) {
        log.info("Deleting warning with id {} in server {}.", warning.getWarnId().getId(), warning.getWarnId().getServerId());
        warnRepository.delete(warning);
    }


}
