package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.moderation.models.BanLog;
import net.dv8tion.jda.api.entities.Member;

public interface BanService {
    void banMember(Member member, String reason);
    void sendBanLog(BanLog banLog);
}
