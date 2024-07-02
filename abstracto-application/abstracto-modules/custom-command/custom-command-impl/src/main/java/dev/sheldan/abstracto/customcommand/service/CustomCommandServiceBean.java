package dev.sheldan.abstracto.customcommand.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.customcommand.exception.CustomCommandExistsException;
import dev.sheldan.abstracto.customcommand.exception.CustomCommandNotFoundException;
import dev.sheldan.abstracto.customcommand.model.database.CustomCommand;
import dev.sheldan.abstracto.customcommand.service.management.CustomCommandManagementService;
import dev.sheldan.abstracto.customcommand.service.management.CustomCommandService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
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

    @Autowired
    private UserManagementService userManagementService;

    @Override
    public CustomCommand createCustomCommand(String name, String content, Member creator) {
        if(customCommandManagementService.getCustomCommandByName(name, creator.getGuild().getIdLong()).isPresent()) {
            throw new CustomCommandExistsException();
        }
        AUserInAServer creatorUser = userInServerManagementService.loadOrCreateUser(creator);
        return customCommandManagementService.createCustomCommand(name, content, creatorUser);
    }

    @Override
    public CustomCommand createUserCustomCommand(String name, String content, User user) {
        AUser aUser = userManagementService.loadOrCreateUser(user.getIdLong());
        if(customCommandManagementService.getUserCustomCommandByName(name, aUser).isPresent()) {
           throw new CustomCommandExistsException();
        }
        return customCommandManagementService.createUserCustomCommand(name, content, aUser);
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
    public void deleteUserCustomCommand(String name, User user) {
        if(customCommandManagementService.getUserCustomCommandByName(name, user.getIdLong()).isEmpty()) {
            throw new CustomCommandNotFoundException();
        }
        AUser aUser = userManagementService.loadOrCreateUser(user.getIdLong());
        customCommandManagementService.deleteCustomCommand(name, aUser);
    }

    @Override
    public List<CustomCommand> getCustomCommands(Guild guild) {
        AServer server = serverManagementService.loadServer(guild);
        return customCommandManagementService.getCustomCommands(server);
    }

    @Override
    public List<CustomCommand> getUserCustomCommands(User user) {
        AUser aUser = userManagementService.loadOrCreateUser(user.getIdLong());
        return customCommandManagementService.getUserCustomCommands(aUser);
    }

    @Override
    public CustomCommand getCustomCommand(String name, Guild guild) {
        return customCommandManagementService.getCustomCommandByName(name, guild.getIdLong())
                .orElseThrow(CustomCommandNotFoundException::new);
    }

    @Override
    public CustomCommand getUserCustomCommand(String name, User user) {
        return customCommandManagementService.getUserCustomCommandByName(name, user.getIdLong())
                .orElseThrow(CustomCommandNotFoundException::new);
    }

    @Override
    public List<CustomCommand> getCustomCommandsStartingWith(String prefix, Guild guild) {
        AServer server = serverManagementService.loadServer(guild);
        return customCommandManagementService.getCustomCommandsStartingWith(prefix, server);
    }

    @Override
    public List<CustomCommand> getUserCustomCommandsStartingWith(String prefix, User user) {
        AUser aUser = userManagementService.loadOrCreateUser(user.getIdLong());
        return customCommandManagementService.getUserCustomCommandsStartingWith(prefix, aUser);
    }
}
