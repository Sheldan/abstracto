package dev.sheldan.abstracto.core.command.execution;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class Parameter {
    private String name;
    private Class type;
    private String description;
    private boolean optional;
    private boolean remainder;
    private Integer maxLength;
}
