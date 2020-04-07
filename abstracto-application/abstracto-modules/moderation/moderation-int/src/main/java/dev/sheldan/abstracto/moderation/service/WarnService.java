package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.moderation.models.template.commands.WarnLog;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Member;


public interface WarnService {
    void warnUser(AUserInAServer warnedAUser, AUserInAServer warningAUser, String reason, WarnLog warnLog);
    void warnUser(Member warnedUser, Member warningUser, String reason, WarnLog warnLog);
}
