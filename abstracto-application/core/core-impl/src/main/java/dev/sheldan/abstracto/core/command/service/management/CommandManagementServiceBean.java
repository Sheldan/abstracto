package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.models.database.AModule;
import dev.sheldan.abstracto.core.command.repository.CommandRepository;
import dev.sheldan.abstracto.core.models.database.AFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandManagementServiceBean implements CommandManagementService {

    @Autowired
    private ModuleManagementService moduleManagementService;

    @Autowired
    private CommandRepository commandRepository;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Override
    public ACommand createCommand(String name, String moduleName, String featureName) {
        AModule module = moduleManagementService.findModuleByName(moduleName);
        AFeature feature = featureManagementService.getFeature(featureName);
        return createCommand(name, module, feature);
    }

    @Override
    public ACommand createCommand(String name, AModule module, AFeature feature) {
        ACommand command = ACommand
                .builder()
                .name(name.toLowerCase())
                .module(module)
                .feature(feature)
                .build();
        commandRepository.save(command);
        return command;
    }

    @Override
    public ACommand findCommandByName(String name) {
        return commandRepository.findByName(name.toLowerCase());
    }

    @Override
    public Boolean doesCommandExist(String name) {
        return findCommandByName(name) != null;
    }

}
