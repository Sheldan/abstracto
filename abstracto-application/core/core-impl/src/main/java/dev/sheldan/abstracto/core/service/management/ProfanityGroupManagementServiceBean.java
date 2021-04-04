package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.ProfanityGroupNotFoundException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ProfanityGroup;
import dev.sheldan.abstracto.core.repository.ProfanityGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ProfanityGroupManagementServiceBean implements ProfanityGroupManagementService {

    @Autowired
    private ProfanityGroupRepository repository;

    @Override
    public List<ProfanityGroup> getAllGroups() {
        return repository.findAll();
    }

    @Override
    public List<ProfanityGroup> getAllForServer(Long serverId) {
        return repository.findByServer_Id(serverId);
    }

    @Override
    public ProfanityGroup createProfanityGroup(AServer server, String name) {
        ProfanityGroup profanityGroup = ProfanityGroup
                .builder()
                .groupName(name)
                .server(server)
                .build();
        log.info("Creating profanity group in server {}.", server.getId());
        return repository.save(profanityGroup);
    }

    @Override
    public boolean doesProfanityGroupExist(AServer server, String name) {
        return getProfanityGroupOptional(server, name).isPresent();
    }

    @Override
    public Optional<ProfanityGroup> getProfanityGroupOptional(AServer server, String name) {
        return repository.findByServerAndGroupNameIgnoreCase(server, name);
    }

    @Override
    public ProfanityGroup getProfanityGroup(AServer server, String name) {
        return getProfanityGroupOptional(server, name).orElseThrow(ProfanityGroupNotFoundException::new);
    }

    @Override
    public void deleteProfanityGroup(ProfanityGroup profanityGroup) {
        repository.delete(profanityGroup);
    }

    @Override
    public void deleteProfanityGroup(AServer server, String name) {
        repository.deleteByServerAndGroupNameIgnoreCase(server, name);
    }
}
