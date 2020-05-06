package dev.sheldan.abstracto.core.command.config;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
public class CommandCreationListener {

    @Autowired
    private List<Command> commandList;

    @Autowired
    private CommandService commandService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureManagementService featureManagementService;


    @EventListener
    @Transactional
    public void handleContextRefreshEvent(ContextRefreshedEvent ctxStartEvt) {
        featureFlagService.getAllFeatureConfigs().forEach((featureFlagKey) -> {
            String featureKey = featureFlagKey.getFeature().getKey();
            if(!featureManagementService.featureExists(featureKey)) {
                featureManagementService.createFeature(featureKey);
            }
        });
        commandList.forEach(command -> {
            if(command.getConfiguration() == null) {
                log.warn("Command {} has null configuration.", command);
                return;
            }
            if(!commandService.doesCommandExist(command.getConfiguration().getName())) {
                commandService.createCommand(command.getConfiguration().getName(), command.getConfiguration().getModule(), command.getFeature());
            }
        });
    }
}
