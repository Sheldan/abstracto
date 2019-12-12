package dev.sheldan.abstracto.command.execution;

import dev.sheldan.abstracto.command.HelpInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Builder
public class Configuration {

    private String name;
    private String module;
    private String description;
    private List<Parameter> parameters;
    private boolean causesReaction;
    private HelpInfo help;

    public long getNecessaryParameterCount(){
        return parameters.stream().filter(parameter -> !parameter.isOptional()).count();
    }
}
