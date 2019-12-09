package dev.sheldan.abstracto.command.execution;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @Builder
public class Configuration {

    private String name;
    private String module;
    private String description;
    private List<Parameter> parameters;
    private boolean causesReaction;
}
