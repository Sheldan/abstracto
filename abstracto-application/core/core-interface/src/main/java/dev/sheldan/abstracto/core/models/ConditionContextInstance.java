package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@Builder
public class ConditionContextInstance {
    private HashMap<String, Object> parameters;
    private String conditionName;
}
