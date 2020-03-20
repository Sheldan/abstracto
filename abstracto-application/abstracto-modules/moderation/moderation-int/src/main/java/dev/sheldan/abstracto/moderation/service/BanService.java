package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerContext;
import net.dv8tion.jda.api.entities.Member;

public interface BanService {
    void banMember(Member member, String reason, ServerContext banLog);
    void banMember(Long guildId, Long userId, String reason, ServerContext banIdLog);
}
