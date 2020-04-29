package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.repository.WarnRepository;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class WarnManagementServiceBean implements WarnManagementService {

    @Autowired
    private WarnRepository warnRepository;

    @Override
    public Warning createWarning(AUserInAServer warnedAUser, AUserInAServer warningAUser, String reason) {
        Warning warning = Warning.builder()
                .reason(reason)
                .warnedUser(warnedAUser)
                .warningUser(warningAUser)
                .warnDate(Instant.now())
                .decayed(false)
                .build();
        warnRepository.save(warning);
        return warning;
    }

    @Override
    public List<Warning> getActiveWarningsInServerOlderThan(AServer server, Instant date) {
        return warnRepository.findAllByWarnedUser_ServerReferenceAndDecayedFalseAndWarnDateLessThan(server, date);
    }
}
