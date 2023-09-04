package dev.sheldan.abstracto.customcommand.service.management;

import dev.sheldan.abstracto.customcommand.model.database.CustomCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public interface CustomCommandService {
    CustomCommand createCustomCommand(String name, String content, Member creator);
    void deleteCustomCommand(String name, Guild guild);
    List<CustomCommand> getCustomCommands(Guild guild);
    CustomCommand getCustomCommand(String name, Guild guild);
}
