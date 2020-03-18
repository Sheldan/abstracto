package dev.sheldan.abstracto.command.execution;

import dev.sheldan.abstracto.command.HelpInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Builder
public class CommandConfiguration {

    private String name;
    private String module;
    private String description;
    private String descriptionTemplate;
    private List<Parameter> parameters;
    private boolean causesReaction;
    private HelpInfo help;

    public int getNecessaryParameterCount(){
        return (int) parameters.stream().filter(parameter -> !parameter.isOptional()).count();
    }
}
