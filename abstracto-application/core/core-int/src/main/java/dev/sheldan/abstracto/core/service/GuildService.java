package dev.sheldan.abstracto.core.service;

import net.dv8tion.jda.api.entities.Guild;

import java.util.Optional;

public interface GuildService {

    Optional<Guild> getGuildByIdOptional(Long serverId);
    Guild getGuildById(Long serverId);
}
