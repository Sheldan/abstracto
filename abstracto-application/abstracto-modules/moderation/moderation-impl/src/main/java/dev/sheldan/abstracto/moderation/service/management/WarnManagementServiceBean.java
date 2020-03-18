package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.moderation.models.Warning;
import dev.sheldan.abstracto.moderation.repository.WarnRepository;
import dev.sheldan.abstracto.core.models.AUserInAServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

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
                .build();
        warnRepository.save(warning);
        return warning;
    }
}
