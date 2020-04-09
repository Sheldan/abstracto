package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.moderation.models.template.commands.BanIdLogModel;
import dev.sheldan.abstracto.moderation.models.template.commands.BanLogModel;
import net.dv8tion.jda.api.entities.Member;

public interface BanService {
    void banMember(Member member, String reason);
    void banMember(Long guildId, Long userId, String reason);
    void sendBanLog(BanLogModel banLogModel);
    void sendBanIdLog(BanIdLogModel banLogModel);
}
