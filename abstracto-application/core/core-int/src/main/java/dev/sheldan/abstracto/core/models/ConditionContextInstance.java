package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class ConditionContextInstance {
    private Map<String, Object> parameters;
    private String conditionName;
}
