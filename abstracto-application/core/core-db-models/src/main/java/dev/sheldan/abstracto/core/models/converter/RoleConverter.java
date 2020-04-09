package dev.sheldan.abstracto.core.models.converter;

import dev.sheldan.abstracto.core.models.ARole;
import dev.sheldan.abstracto.core.models.dto.RoleDto;
import org.springframework.stereotype.Component;

@Component
public class RoleConverter {
    public RoleDto fromARole(ARole role) {
        return RoleDto
                .builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }

    public ARole fromDto(RoleDto role) {
        return ARole
                .builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }
}
