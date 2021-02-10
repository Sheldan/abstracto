package dev.sheldan.abstracto.core.models.property;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SystemConfigProperty {
    private String name;
    private Long longValue;
    private String stringValue;
    private Double doubleValue;
}
