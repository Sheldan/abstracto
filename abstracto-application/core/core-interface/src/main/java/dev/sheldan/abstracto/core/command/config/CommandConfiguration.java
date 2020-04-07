package dev.sheldan.abstracto.core.command.config;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class CommandConfiguration {

    private String name;
    private String module;
    private String description;
    private List<Parameter> parameters;
    private List<String> aliases;
    private boolean causesReaction;
    private boolean templated;
    private HelpInfo help;

    public int getNecessaryParameterCount(){
        return (int) parameters.stream().filter(parameter -> !parameter.isOptional()).count();
    }
}
