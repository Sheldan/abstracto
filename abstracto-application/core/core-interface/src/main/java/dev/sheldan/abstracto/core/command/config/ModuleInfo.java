package dev.sheldan.abstracto.core.command.config;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ModuleInfo {

    private String name;
    private String description;
    private Boolean templated;
}
