package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.model.database.InfractionParameter;
import dev.sheldan.abstracto.moderation.model.database.embedded.InfractionParameterId;
import dev.sheldan.abstracto.moderation.repository.InfractionParameterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InfractionParameterManagementServiceBean implements InfractionParameterManagementService{

    @Autowired
    private InfractionParameterRepository infractionParameterRepository;

    @Override
    public InfractionParameter createInfractionParameter(Infraction infraction, String key, String value) {
        InfractionParameterId id = new InfractionParameterId(infraction.getId(), key);
        InfractionParameter parameter = InfractionParameter
                .builder()
                .infractionParameterId(id)
                .value(value)
                .infraction(infraction)
                .build();
        return infractionParameterRepository.save(parameter);
    }
}
