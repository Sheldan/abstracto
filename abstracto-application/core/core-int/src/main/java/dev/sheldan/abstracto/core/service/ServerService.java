package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import net.dv8tion.jda.api.entities.Guild;

public interface ServerService {
    boolean adminModeActive(Long id);
    boolean adminModeActive(Guild guild);
    void setAdminModeTo(Long id, Boolean newState);
    void setAdminModeTo(Guild guild, Boolean newState);
    void setAdminModeTo(AServer server, Boolean newState);
    void activateAdminMode(Long id);
    void deactivateAdminMode(Long id);
}
