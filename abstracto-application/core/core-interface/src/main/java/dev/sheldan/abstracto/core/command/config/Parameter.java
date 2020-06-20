package dev.sheldan.abstracto.core.command.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class Parameter {
    private String name;
    private Class type;
    private String description;
    @Builder.Default
    private boolean optional = false;
    @Builder.Default
    private boolean remainder = false;
    private Integer maxLength;
    @Builder.Default
    private Boolean templated = false;
}
