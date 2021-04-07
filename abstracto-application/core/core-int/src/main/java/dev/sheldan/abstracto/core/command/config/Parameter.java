package dev.sheldan.abstracto.core.command.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Builder.Default
    private Map<String, Object> additionalInfo = new HashMap<>();

    public static final String ADDITIONAL_TYPES_KEY = "ADDITIONAL_TYPES";
}
