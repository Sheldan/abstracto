package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.moderation.models.KickLogModel;
import net.dv8tion.jda.api.entities.Member;

public interface KickService {
    void kickMember(Member member, String reason);
    void sendKickLog(KickLogModel kickLogModel);
}
