package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.exception.AssignableRolePlaceNotFoundException;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.repository.AssignableRolePlaceRepository;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class AssignableRolePlaceManagementServiceBean implements AssignableRolePlaceManagementService {

    @Autowired
    private AssignableRolePlaceRepository repository;

    @Override
    public AssignableRolePlace createPlace(String name, AChannel channel, String text) {
        AssignableRolePlace place = AssignableRolePlace
                .builder()
                .channel(channel)
                .server(channel.getServer())
                .text(text)
                .key(name)
                .build();
        log.info("Creating assignable role place in channel {} on server {}.", channel.getId(), channel.getServer().getId());
        return repository.save(place);
    }

    @Override
    public boolean doesPlaceExist(AServer server, String name) {
        return repository.existsByServerAndKey(server, name);
    }

    @Override
    public AssignableRolePlace findByServerAndKey(AServer server, String name) {
        return repository.findByServerAndKey(server, name).orElseThrow(AssignableRolePlaceNotFoundException::new);
    }

    @Override
    public Optional<AssignableRolePlace> findByPlaceIdOptional(Long id) {
        return repository.findById(id);
    }

    @Override
    public AssignableRolePlace findByPlaceId(Long id) {
        return findByPlaceIdOptional(id).orElseThrow(AssignableRolePlaceNotFoundException::new);
    }

    @Override
    public void moveAssignableRolePlace(String name, AChannel newChannel) {
        AssignableRolePlace assignablePlaceToChange = findByServerAndKey(newChannel.getServer(), name);
        log.info("Moving assignable role place {} in server {} from channel {} to channel {}.",
                assignablePlaceToChange.getId(), newChannel.getServer().getId(), assignablePlaceToChange.getChannel().getId(), newChannel.getId());
        assignablePlaceToChange.setChannel(newChannel);
    }

    @Override
    public void changeAssignableRolePlaceDescription(AServer server, String name, String newDescription) {
        AssignableRolePlace assignablePlaceToChange = findByServerAndKey(server, name);
        log.info("Changing description of assignable role place {} in server {}.", assignablePlaceToChange.getId(), server.getId());
        assignablePlaceToChange.setText(newDescription);
    }

    @Override
    public void deleteAssignablePlace(AssignableRolePlace toDelete) {
        log.info("Deleting assignable role place {} in server {} which was in server {}.", toDelete.getId(), toDelete.getChannel().getId(), toDelete.getServer().getId());
        repository.delete(toDelete);
    }

    @Override
    public List<AssignableRolePlace> findAllByServer(AServer server) {
        return repository.findByServer(server);
    }


}
