package dev.sheldan.abstracto.core.service;

import net.dv8tion.jda.api.entities.Guild;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ApplicationCommandServiceBean implements ApplicationCommandService {

    @Override
    public CompletableFuture<Void> deleteGuildCommand(Guild guild, Long commandId) {
        return guild.deleteCommandById(commandId).submit();
    }
}
