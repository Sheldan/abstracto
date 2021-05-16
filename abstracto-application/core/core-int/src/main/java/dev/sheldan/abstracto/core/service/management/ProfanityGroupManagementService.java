package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ProfanityGroup;

import java.util.List;
import java.util.Optional;

public interface ProfanityGroupManagementService {
    List<ProfanityGroup> getAllGroups();
    List<ProfanityGroup> getAllForServer(Long serverId);
    ProfanityGroup createProfanityGroup(AServer server, String name);
    boolean doesProfanityGroupExist(AServer server, String name);
    Optional<ProfanityGroup> getProfanityGroupByIdOptional(Long profanityGroupId);
    ProfanityGroup getProfanityGroupById(Long profanityGroupId);
    Optional<ProfanityGroup> getProfanityGroupOptional(AServer server, String name);
    ProfanityGroup getProfanityGroup(AServer server, String name);
    void deleteProfanityGroup(ProfanityGroup profanityGroup);
    void deleteProfanityGroup(AServer server, String name);
}
