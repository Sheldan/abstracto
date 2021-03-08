package dev.sheldan.abstracto.scheduling.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
public class JobParameters {
    @Builder.Default
    private Map<Object, Object> parameters = new HashMap<>();
}
