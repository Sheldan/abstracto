package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnLog;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;


public interface WarnService {
    Warning warnUser(AUserInAServer warnedAUser, AUserInAServer warningAUser, String reason, TextChannel feedbackChannel);
    Warning warnUser(Member warnedMember, Member warningMember, String reason, TextChannel feedbackChannel);
    Warning warnUser(FullUser warnedUser, FullUser warningUser, String reason, TextChannel feedbackChannel);
    void warnUserWithLog(Member warnedMember, Member warningMember, String reason, WarnLog warnLog, TextChannel feedbackChannel);
}
