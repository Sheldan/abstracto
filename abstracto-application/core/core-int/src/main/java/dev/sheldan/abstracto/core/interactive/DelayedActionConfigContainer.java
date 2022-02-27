package dev.sheldan.abstracto.core.interactive;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DelayedActionConfigContainer {
    private Class type;
    private DelayedActionConfig object;
    private String config;
}
