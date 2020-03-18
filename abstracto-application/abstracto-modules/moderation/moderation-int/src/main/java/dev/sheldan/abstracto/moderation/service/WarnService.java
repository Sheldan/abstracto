package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.moderation.models.WarnLog;
import dev.sheldan.abstracto.moderation.models.Warning;
import dev.sheldan.abstracto.core.models.AUserInAServer;
import net.dv8tion.jda.api.entities.Member;


public interface WarnService {
    Warning warnUser(AUserInAServer warnedAUser, AUserInAServer warningAUser, String reason);
    Warning warnUser(Member warnedUser, Member warningUser, String reason);
    void sendWarnLog(WarnLog warnLogModel);
}
