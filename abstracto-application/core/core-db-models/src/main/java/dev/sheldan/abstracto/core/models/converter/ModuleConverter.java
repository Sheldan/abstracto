package dev.sheldan.abstracto.core.models.converter;

import dev.sheldan.abstracto.core.models.AModule;
import dev.sheldan.abstracto.core.models.dto.CommandDto;
import dev.sheldan.abstracto.core.models.dto.ModuleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ModuleConverter {

    @Autowired
    private CommandConverter commandConverter;

    public ModuleDto fromModule(AModule module) {
        List<CommandDto> commands = new ArrayList<>();
        module.getCommands().forEach(command -> {
            commands.add(commandConverter.fromCommand(command));
        });
        return ModuleDto.builder().name(module.getName()).id(module.getId()).commands(commands).build();
    }
}
