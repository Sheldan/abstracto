package dev.sheldan.abstracto.core.interaction.context.management;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.ContextCommand;
import dev.sheldan.abstracto.core.models.database.ContextType;
import dev.sheldan.abstracto.core.repository.ContextCommandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContextCommandManagementServiceBean implements ContextCommandManagementService {

    @Autowired
    private ContextCommandRepository contextCommandRepository;

    @Override
    public ContextCommand createContextCommand(String name, ContextType contextType, AFeature feature) {
        ContextCommand contextCommand = ContextCommand
                .builder()
                .commandName(name)
                .type(contextType)
                .feature(feature)
                .build();
        return contextCommandRepository.save(contextCommand);
    }

    @Override
    public ContextCommand findContextCommand(String name) {
        return contextCommandRepository.findByCommandName(name)
                .orElseThrow(() -> new AbstractoRunTimeException("Context command not found."));
    }
}
