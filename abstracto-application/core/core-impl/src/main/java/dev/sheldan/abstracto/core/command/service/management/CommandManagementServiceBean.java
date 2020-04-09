package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.models.ACommand;
import dev.sheldan.abstracto.core.models.AModule;
import dev.sheldan.abstracto.core.command.repository.CommandRepository;
import dev.sheldan.abstracto.core.models.converter.CommandConverter;
import dev.sheldan.abstracto.core.models.converter.ModuleConverter;
import dev.sheldan.abstracto.core.models.dto.CommandDto;
import dev.sheldan.abstracto.core.models.dto.ModuleDto;
import dev.sheldan.abstracto.core.service.management.ServerManagementServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandManagementServiceBean {

    @Autowired
    private ServerManagementServiceBean serverManagementService;

    @Autowired
    private ModuleManagementServiceBean moduleManagementService;

    @Autowired
    private CommandRepository commandRepository;

    @Autowired
    private CommandConverter commandConverter;

    @Autowired
    private ModuleConverter moduleConverter;

    public CommandDto createCommand(String name, String moduleName) {
        AModule module = moduleManagementService.findModuleByName(moduleName);
        return createCommand(name, moduleConverter.fromModule(module));
    }

    public CommandDto createCommand(String name, ModuleDto module) {
        ACommand command = ACommand
                .builder()
                .name(name)
                .module(AModule.builder().id(module.getId()).build())
                .build();
        commandRepository.save(command);
        return commandConverter.fromCommand(command);
    }

    public CommandDto findCommandByName(String name) {
        ACommand byName = commandRepository.findByName(name);
        return commandConverter.fromCommand(byName);
    }

    public Boolean doesCommandExist(String name) {
        return findCommandByName(name) != null;
    }

}
