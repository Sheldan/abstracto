package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.exceptions.AssignableRolePlaceNotFoundException;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.repository.AssignableRolePlaceRepository;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AssignableRolePlaceManagementServiceBean implements AssignableRolePlaceManagementService {

    @Autowired
    private AssignableRolePlaceRepository repository;

    @Override
    public AssignableRolePlace createPlace(AServer server, String name, AChannel channel, String text) {
        AssignableRolePlace place = AssignableRolePlace
                .builder()
                .channel(channel)
                .server(server)
                .text(text)
                .key(name)
                .build();
        repository.save(place);
        return place;
    }

    @Override
    public boolean doesPlaceExist(AServer server, String name) {
        return repository.existsByServerAndKey(server, name);
    }

    @Override
    public AssignableRolePlace findByServerAndKey(AServer server, String name) {
        // todo use other exception or adapt exception
        return repository.findByServerAndKey(server, name).orElseThrow(() -> new AssignableRolePlaceNotFoundException(0L));
    }

    @Override
    public Optional<AssignableRolePlace> findByPlaceIdOptional(Long id) {
        return repository.findById(id);
    }

    @Override
    public AssignableRolePlace findByPlaceId(Long id) {
        return findByPlaceIdOptional(id).orElseThrow(() -> new AssignableRolePlaceNotFoundException(id));
    }

    @Override
    public void moveAssignableRolePlace(AServer server, String name, AChannel newChannel) {
        AssignableRolePlace assignablePlaceToChange = findByServerAndKey(server, name);
        assignablePlaceToChange.setChannel(newChannel);
    }

    @Override
    public void changeAssignableRolePlaceDescription(AServer server, String name, String newDescription) {
        AssignableRolePlace assignablePlaceToChange = findByServerAndKey(server, name);
        assignablePlaceToChange.setText(newDescription);
    }

    @Override
    public void deleteAssignablePlace(AssignableRolePlace toDelete) {
        repository.delete(toDelete);
    }

    @Override
    public List<AssignableRolePlace> findAllByServer(AServer server) {
        return repository.findByServer(server);
    }


}
