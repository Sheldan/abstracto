package dev.sheldan.abstracto.core.command.execution;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
public class Parameters {
    @Getter
    private List<Object> parameters;
}
