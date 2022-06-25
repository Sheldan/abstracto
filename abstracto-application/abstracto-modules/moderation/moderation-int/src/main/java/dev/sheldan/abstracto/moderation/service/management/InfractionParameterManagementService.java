package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.model.database.InfractionParameter;

public interface InfractionParameterManagementService {
    InfractionParameter createInfractionParameter(Infraction infraction, String key, String value);
}
