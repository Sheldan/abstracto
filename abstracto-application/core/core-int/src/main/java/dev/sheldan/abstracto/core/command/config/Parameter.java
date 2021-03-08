package dev.sheldan.abstracto.core.command.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class Parameter implements Serializable {
    private String name;
    private Class type;
    private String description;
    @Builder.Default
    private boolean optional = false;
    @Builder.Default
    private boolean remainder = false;
    @Builder.Default
    private boolean isListParam = false;
    @Builder.Default
    private Boolean templated = false;
    @Builder.Default
    private List<ParameterValidator> validators = new ArrayList<>();
}
