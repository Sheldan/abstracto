package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.Infraction;

import java.util.List;

public interface InfractionManagementService {
    Infraction createInfraction(AUserInAServer aUserInAServer, Long points);
    List<Infraction> getActiveInfractionsForUser(AUserInAServer aUserInAServer);
    Infraction loadInfraction(Long infraction);
}
