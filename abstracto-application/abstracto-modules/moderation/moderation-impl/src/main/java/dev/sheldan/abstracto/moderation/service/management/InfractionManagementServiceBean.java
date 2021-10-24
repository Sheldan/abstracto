package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.repository.InfractionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InfractionManagementServiceBean implements InfractionManagementService{

    @Autowired
    private InfractionRepository infractionRepository;

    @Override
    public Infraction createInfraction(AUserInAServer aUserInAServer, Long points) {
        Infraction infraction = Infraction
                .builder()
                .user(aUserInAServer)
                .server(aUserInAServer.getServerReference())
                .points(points)
                .build();
        return infractionRepository.save(infraction);
    }

    @Override
    public List<Infraction> getActiveInfractionsForUser(AUserInAServer aUserInAServer) {
        return infractionRepository.findByUserAndDecayedFalse(aUserInAServer);
    }

    @Override
    public Infraction loadInfraction(Long infraction) {
        return infractionRepository.getOne(infraction);
    }

}
