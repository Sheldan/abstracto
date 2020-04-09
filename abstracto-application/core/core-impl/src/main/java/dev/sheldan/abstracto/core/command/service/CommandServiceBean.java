package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.models.ACommand;
import dev.sheldan.abstracto.core.models.AModule;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementServiceBean;
import dev.sheldan.abstracto.core.command.service.management.ModuleManagementServiceBean;
import dev.sheldan.abstracto.core.models.converter.CommandConverter;
import dev.sheldan.abstracto.core.models.dto.CommandDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandServiceBean implements CommandService {

    @Autowired
    private ModuleManagementServiceBean moduleManagementService;

    @Autowired
    private CommandManagementServiceBean commandManagementServiceBean;

    @Autowired
    private CommandConverter commandConverter;

    @Override
    public CommandDto createCommand(String name, String moduleName) {
        return commandManagementServiceBean.createCommand(name, moduleName);
    }

    @Override
    public Boolean doesCommandExist(String name) {
        return commandManagementServiceBean.doesCommandExist(name);
    }

    @Override
    public CommandDto findCommandByName(String name) {
        return commandManagementServiceBean.findCommandByName(name);
    }


}
