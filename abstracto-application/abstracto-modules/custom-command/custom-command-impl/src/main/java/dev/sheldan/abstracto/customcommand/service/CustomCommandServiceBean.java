package dev.sheldan.abstracto.customcommand.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.customcommand.exception.CustomCommandExistsException;
import dev.sheldan.abstracto.customcommand.exception.CustomCommandNotFoundException;
import dev.sheldan.abstracto.customcommand.model.database.CustomCommand;
import dev.sheldan.abstracto.customcommand.service.management.CustomCommandManagementService;
import dev.sheldan.abstracto.customcommand.service.management.CustomCommandService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomCommandServiceBean implements CustomCommandService {

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private CustomCommandManagementService customCommandManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CustomCommand createCustomCommand(String name, String content, Member creator) {
        if(customCommandManagementService.getCustomCommandByName(name, creator.getGuild().getIdLong()).isPresent()) {
            throw new CustomCommandExistsException();
        }
        AUserInAServer creatorUser = userInServerManagementService.loadOrCreateUser(creator);
        return customCommandManagementService.createCustomCommand(name, content, creatorUser);
    }

    @Override
    public void deleteCustomCommand(String name, Guild guild) {
        if(customCommandManagementService.getCustomCommandByName(name, guild.getIdLong()).isEmpty()) {
            throw new CustomCommandNotFoundException();
        }
        AServer server = serverManagementService.loadServer(guild);
        customCommandManagementService.deleteCustomCommand(name, server);
    }

    @Override
    public List<CustomCommand> getCustomCommands(Guild guild) {
        AServer server = serverManagementService.loadServer(guild);
        return customCommandManagementService.getCustomCommands(server);
    }

    @Override
    public CustomCommand getCustomCommand(String name, Guild guild) {
        return customCommandManagementService.getCustomCommandByName(name, guild.getIdLong())
                .orElseThrow(CustomCommandNotFoundException::new);
    }
}
