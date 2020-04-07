package dev.sheldan.abstracto.core.command.config;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
public class Parameters {
    @Getter
    private List<Object> parameters;
}
