package dev.sheldan.abstracto.core.command.config;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandConfigListener implements ServerConfigListener {

    @Autowired
    private List<Command> commandList;

    @Autowired
    private CommandService commandService;

    @Override
    public void updateServerConfig(AServer server) {
        commandList.forEach(command -> {
            if(!commandService.doesCommandExist(command.getConfiguration().getName())) {
                commandService.createCommand(command.getConfiguration().getName(), command.getConfiguration().getModule());
            }
        });
    }
}
