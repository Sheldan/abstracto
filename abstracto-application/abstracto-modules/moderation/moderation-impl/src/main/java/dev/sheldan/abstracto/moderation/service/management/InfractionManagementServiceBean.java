package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.repository.InfractionRepository;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InfractionManagementServiceBean implements InfractionManagementService {

    @Autowired
    private InfractionRepository infractionRepository;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public Infraction createInfraction(AUserInAServer target, Long points, String type, String description, AUserInAServer creator, Message message) {
        AChannel channel;
        if(message != null) {
            channel = channelManagementService.loadChannel(message.getChannel().getIdLong());
        } else {
            channel = null;
        }
        Infraction infraction = Infraction
                .builder()
                .user(target)
                .infractionCreator(creator)
                .server(target.getServerReference())
                .decayed(false)
                .logChannel(channel)
                .logMessageId(message != null ? message.getIdLong() : null)
                .type(type)
                .description(description)
                .points(points)
                .build();
        return infractionRepository.save(infraction);
    }

    @Override
    public List<Infraction> getActiveInfractionsForUser(AUserInAServer aUserInAServer) {
        return infractionRepository.findByUserAndDecayedFalse(aUserInAServer);
    }

    @Override
    public List<Infraction> getInfractionsForUser(AUserInAServer aUserInAServer) {
        return infractionRepository.findByUserOrderByCreated(aUserInAServer);
    }

    @Override
    public List<Infraction> getInfractionsForServer(AServer server) {
        return infractionRepository.findByServerOrderByCreated(server);
    }

    @Override
    public Infraction loadInfraction(Long infraction) {
        return infractionRepository.getOne(infraction);
    }

}
