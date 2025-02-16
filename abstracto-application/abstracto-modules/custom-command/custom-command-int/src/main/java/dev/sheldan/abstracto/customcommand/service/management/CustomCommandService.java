package dev.sheldan.abstracto.customcommand.service.management;

import dev.sheldan.abstracto.customcommand.model.database.CustomCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public interface CustomCommandService {
    CustomCommand createCustomCommand(String name, String content, Member creator);
    CustomCommand createUserCustomCommand(String name, String content, User user);
    void deleteCustomCommand(String name, Guild guild);
    void deleteUserCustomCommand(String name, User user);
    List<CustomCommand> getCustomCommands(Guild guild);
    List<CustomCommand> getUserCustomCommands(User user);
    CustomCommand getCustomCommand(String name, Guild guild);
    CustomCommand getUserCustomCommand(String name, User user);
    List<CustomCommand> getCustomCommandsContaining(String name, Guild guild);
    List<CustomCommand> getUserCustomCommandsContaining(String name, User user);
}
