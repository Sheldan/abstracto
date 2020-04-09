package dev.sheldan.abstracto.core.models.converter;

import dev.sheldan.abstracto.core.models.ACommand;
import dev.sheldan.abstracto.core.models.dto.CommandDto;
import org.springframework.stereotype.Component;

@Component
public class CommandConverter {

    public CommandDto fromCommand(ACommand command) {
        return CommandDto
                .builder()
                .name(command.getName())
                .id(command.getId())
                .build();
    }

    public ACommand toCommand(CommandDto command) {
        return ACommand
                .builder()
                .name(command.getName())
                .id(command.getId())
                .build();
    }

}
