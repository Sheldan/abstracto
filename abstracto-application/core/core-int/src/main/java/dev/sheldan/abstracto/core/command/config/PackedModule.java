package dev.sheldan.abstracto.core.command.config;

import dev.sheldan.abstracto.core.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PackedModule {
    private ModuleDefinition moduleDefinition;
    private PackedModule parentModule;
    private List<PackedModule> subModules;
    private List<Command> commands;
}
