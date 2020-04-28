package dev.sheldan.abstracto.core.command.config;

import dev.sheldan.abstracto.core.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class SingleLevelPackedModule {
    private ModuleInterface moduleInterface;
    private List<Command> commands;
}
