package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.AUserInAServer;
import dev.sheldan.abstracto.moderation.converter.WarnConverter;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.models.dto.WarnDto;
import dev.sheldan.abstracto.moderation.repository.WarnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class WarnManagementServiceBean  {

    @Autowired
    private WarnRepository warnRepository;

    @Autowired
    private WarnConverter warnConverter;

    public WarnDto createWarning(AUserInAServer warnedAUser, AUserInAServer warningAUser, String reason) {
        Warning warning = Warning.builder()
                .reason(reason)
                .warnedUser(warnedAUser)
                .warningUser(warningAUser)
                .warnDate(Instant.now())
                .build();
        warnRepository.save(warning);
        return warnConverter.convertFromAWarn(warning);
    }
}
