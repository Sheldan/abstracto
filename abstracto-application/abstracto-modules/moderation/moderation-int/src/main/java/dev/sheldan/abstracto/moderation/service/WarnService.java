package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerContext;
import dev.sheldan.abstracto.moderation.models.template.WarnLog;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Member;


public interface WarnService {
    void warnUser(AUserInAServer warnedAUser, AUserInAServer warningAUser, String reason, ServerContext warnLog);
    void warnUser(Member warnedUser, Member warningUser, String reason, ServerContext warnLog);
}
