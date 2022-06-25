package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public interface InfractionManagementService {
    Infraction createInfraction(AUserInAServer aUserInAServer, Long points, String type, String description, AUserInAServer creator, Message message);
    List<Infraction> getActiveInfractionsForUser(AUserInAServer aUserInAServer);
    List<Infraction> getInfractionsForUser(AUserInAServer aUserInAServer);
    List<Infraction> getInfractionsForServer(AServer server);
    Infraction loadInfraction(Long infraction);
}
