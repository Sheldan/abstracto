package dev.sheldan.abstracto.core.models.dto;

import dev.sheldan.abstracto.core.models.AModule;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommandDto {
    private Long id;
    private String name;
    private AModule module;
}
