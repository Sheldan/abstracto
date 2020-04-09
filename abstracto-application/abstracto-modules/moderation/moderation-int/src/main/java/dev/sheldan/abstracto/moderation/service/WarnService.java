package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import dev.sheldan.abstracto.moderation.models.dto.WarnDto;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnLogModel;
import net.dv8tion.jda.api.entities.Member;


public interface WarnService {
    WarnDto warnUser(UserInServerDto warnedAUser, UserInServerDto warningAUser, String reason);
    WarnDto warnUser(Member warnedUser, Member warningUser, String reason);
    void sendWarnLog(WarnLogModel warnLogModel);
}
