package dev.sheldan.abstracto.profanityfilter.service.management;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUserInAServer;
import dev.sheldan.abstracto.profanityfilter.repository.ProfanityUserInServerRepository;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProfanityUserInServerManagementServiceBean implements ProfanityUserInServerManagementService {

    @Autowired
    private ProfanityUserInServerRepository repository;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public Optional<ProfanityUserInAServer> getProfanityUserOptional(Member member) {
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(member);
        return getProfanityUserOptional(userInAServer);
    }

    @Override
    public Optional<ProfanityUserInAServer> getProfanityUserOptional(AUserInAServer aUserInAServer) {
        return repository.findById(aUserInAServer.getUserInServerId());
    }

    @Override
    public ProfanityUserInAServer getProfanityUser(AUserInAServer aUserInAServer) {
        return getProfanityUserOptional(aUserInAServer).orElseThrow(() -> new AbstractoRunTimeException("Profanity user in server not found."));
    }

    @Override
    public ProfanityUserInAServer createProfanityUser(AUserInAServer aUserInAServer) {
        ProfanityUserInAServer profanityUserInAServer = ProfanityUserInAServer
                .builder()
                .user(aUserInAServer)
                .id(aUserInAServer.getUserInServerId())
                .server(aUserInAServer.getServerReference())
                .build();
        return repository.save(profanityUserInAServer);
    }

    @Override
    public ProfanityUserInAServer getOrCreateProfanityUser(AUserInAServer aUserInAServer) {
        Optional<ProfanityUserInAServer> profanityUserOptional = getProfanityUserOptional(aUserInAServer);
        return profanityUserOptional.orElseGet(() -> createProfanityUser(aUserInAServer));
    }


}
