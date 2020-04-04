package dev.sheldan.abstracto.core.command.module;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class ModuleInfo {

    private String name;
    private String description;
}
