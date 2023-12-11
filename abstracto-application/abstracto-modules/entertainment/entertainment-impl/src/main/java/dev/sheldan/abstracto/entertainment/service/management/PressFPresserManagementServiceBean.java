package dev.sheldan.abstracto.entertainment.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.entertainment.model.database.PressF;
import dev.sheldan.abstracto.entertainment.model.database.PressFPresser;
import dev.sheldan.abstracto.entertainment.model.database.embed.PressFPresserId;
import dev.sheldan.abstracto.entertainment.repository.PressFPresserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PressFPresserManagementServiceBean implements PressFPresserManagementService {

    @Autowired
    private PressFPresserRepository repository;

    @Override
    public PressFPresser addPresser(PressF pressF, AUserInAServer presser) {
        PressFPresser pressFPresser = PressFPresser
                .builder()
                .presser(presser)
                .id(new PressFPresserId(presser.getUserInServerId(), pressF.getId()))
                .build();
        return repository.save(pressFPresser);
    }

    @Override
    public boolean didUserAlreadyPress(PressF pressF, AUserInAServer aUserInAServer) {
        return repository.existsById(new PressFPresserId(aUserInAServer.getUserInServerId(), pressF.getId()));
    }

}
