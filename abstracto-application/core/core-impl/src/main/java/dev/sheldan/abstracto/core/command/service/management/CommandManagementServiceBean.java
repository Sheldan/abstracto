package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.exception.CommandNotFoundException;
import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.models.database.AModule;
import dev.sheldan.abstracto.core.command.repository.CommandRepository;
import dev.sheldan.abstracto.core.models.database.AFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
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
        log.info("Creating creating command {} in module {} with feature {}.", name, module.getName(), feature.getKey());
        return commandRepository.save(command);
    }

    @Override
    public Optional<ACommand> findCommandByNameOptional(String name) {
        return commandRepository.findByNameIgnoreCase(name.toLowerCase());
    }

    @Override
    public ACommand findCommandByName(String name) {
        return findCommandByNameOptional(name).orElseThrow(CommandNotFoundException::new);
    }

    @Override
    public boolean doesCommandExist(String name) {
        return commandRepository.existsByNameIgnoreCase(name.toLowerCase());
    }

}
