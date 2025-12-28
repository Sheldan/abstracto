package dev.sheldan.abstracto.core.models.template.commands;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GetConfigModel {
    private List<ConfigValue> values;


    @Getter
    @Builder
    public static class ConfigValue {
        private String key;
        @Builder.Default
        private Boolean hasConcreateValue = false;
        private Long longValue;
        private String stringValue;
        private Double doubleValue;
        private ConfigValue defaultValue;
    }

}
