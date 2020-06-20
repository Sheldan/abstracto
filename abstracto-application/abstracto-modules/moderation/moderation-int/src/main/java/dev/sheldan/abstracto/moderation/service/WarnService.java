package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnLog;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.time.Instant;


public interface WarnService {
    Warning warnUser(AUserInAServer warnedAUser, AUserInAServer warningAUser, String reason, MessageChannel feedbackChannel);
    Warning warnMember(Member warnedMember, Member warningMember, String reason, MessageChannel feedbackChannel);
    Warning warnFullUser(FullUser warnedUser, FullUser warningUser, String reason, MessageChannel feedbackChannel);
    Warning warnUserWithLog(Member warnedMember, Member warningMember, String reason, WarnLog warnLog, MessageChannel feedbackChannel);
    void decayWarning(Warning warning, Instant decayDate);
    void decayWarningsForServer(AServer server);
    void decayAllWarningsForServer(AServer server, boolean logWarnings);
}
