package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.models.database.MuteRole;
import dev.sheldan.abstracto.moderation.repository.MuteRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MuteRoleManagementServiceBean implements MuteRoleManagementService {

    @Autowired
    private MuteRoleRepository muteRoleRepository;

    @Override
    public MuteRole retrieveMuteRoleForServer(AServer server) {
        return muteRoleRepository.findByRoleServer(server);
    }

    @Override
    public MuteRole createMuteRoleForServer(AServer server, ARole role) {
        log.trace("Creating mute role for server {} to be role {}", server.getId(), role.getId());
        MuteRole muteRole = MuteRole
                .builder()
                .role(role)
                .roleServer(server)
                .build();
        muteRoleRepository.save(muteRole);
        return  muteRole;
    }

    @Override
    public List<MuteRole> retrieveMuteRolesForServer(AServer server) {
        return muteRoleRepository.findAllByRoleServer(server);
    }

    @Override
    public MuteRole setMuteRoleForServer(AServer server, ARole role) {
        log.info("Setting muted role for server {} to role {}", server.getId(), role.getId());
        MuteRole existing = retrieveMuteRoleForServer(server);
        if(existing == null) {
            return createMuteRoleForServer(server, role);
        } else {
            log.trace("Updating mute role for server {} to be role {} instead.", server.getId(), role.getId());
            existing.setRole(role);
            return existing;
        }
    }

    @Override
    public boolean muteRoleForServerExists(AServer server) {
        return retrieveMuteRoleForServer(server) != null;
    }
}
