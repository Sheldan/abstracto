package dev.sheldan.abstracto.core.command.config;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class Parameters {
    private List<Object> parameters;
}
