package dev.sheldan.abstracto.core.models.exception;

import dev.sheldan.abstracto.core.models.database.ARole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class RoleDeletedExceptionModel {
    private final ARole role;
}
