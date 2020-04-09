package dev.sheldan.abstracto.core.models.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class ModuleDto {
    private Long id;

    private String name;

    private List<CommandDto> commands = new ArrayList<>();
}
