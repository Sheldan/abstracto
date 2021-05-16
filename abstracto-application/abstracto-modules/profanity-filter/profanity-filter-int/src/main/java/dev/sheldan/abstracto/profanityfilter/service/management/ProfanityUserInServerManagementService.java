package dev.sheldan.abstracto.profanityfilter.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUserInAServer;
import net.dv8tion.jda.api.entities.Member;

import java.util.Optional;

public interface ProfanityUserInServerManagementService {
    Optional<ProfanityUserInAServer> getProfanityUserOptional(Member member);
    Optional<ProfanityUserInAServer> getProfanityUserOptional(AUserInAServer aUserInAServer);
    ProfanityUserInAServer getProfanityUser(AUserInAServer aUserInAServer);
    ProfanityUserInAServer createProfanityUser(AUserInAServer aUserInAServer);
    ProfanityUserInAServer getOrCreateProfanityUser(AUserInAServer aUserInAServer);
}
