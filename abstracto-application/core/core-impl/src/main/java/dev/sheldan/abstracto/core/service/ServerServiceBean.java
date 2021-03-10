package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServerServiceBean implements ServerService {

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public boolean adminModeActive(Long id) {
        return serverManagementService.loadServer(id).getAdminMode();
    }

    @Override
    public boolean adminModeActive(Guild guild) {
        return adminModeActive(guild.getIdLong());
    }

    @Override
    public void setAdminModeTo(Long id, Boolean newState) {
        AServer server = serverManagementService.loadServer(id);
        server.setAdminMode(newState);
    }

    @Override
    public void setAdminModeTo(Guild guild, Boolean newState) {
        setAdminModeTo(guild.getIdLong(), newState);
    }

    @Override
    public void setAdminModeTo(AServer server, Boolean newState) {
        server.setAdminMode(newState);
    }

    @Override
    public void activateAdminMode(Long id) {
        setAdminModeTo(id, true);
    }

    @Override
    public void deactivateAdminMode(Long id) {
        setAdminModeTo(id, false);
    }
}
