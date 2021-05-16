package dev.sheldan.abstracto.profanityfilter.service.management;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.database.ProfanityGroup;
import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUse;
import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUserInAServer;

import java.util.List;
import java.util.Optional;

public interface ProfanityUseManagementService {
    ProfanityUse createProfanityUse(ServerChannelMessage profaneMessage, ServerChannelMessage reportMessage, ProfanityUserInAServer reportedUser, ProfanityGroup usedProfanityGroup);
    Optional<ProfanityUse> getProfanityUseViaReportMessageId(Long messageId);
    Long getPositiveReports(ProfanityUserInAServer profanityUserInAServer);
    Long getFalsePositiveReports(ProfanityUserInAServer profanityUserInAServer);
    List<ProfanityUse> getMostRecentProfanityReports(ProfanityUserInAServer profanityUserInAServer, int count);
}
