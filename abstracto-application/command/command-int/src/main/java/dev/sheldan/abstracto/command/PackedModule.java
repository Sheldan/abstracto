package dev.sheldan.abstracto.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PackedModule {
    private Module module;
    private PackedModule parentModule;
    private List<PackedModule> subModules;
    private List<Command> commands;
}
