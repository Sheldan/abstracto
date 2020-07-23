package dev.sheldan.abstracto.core.models.exception;

import dev.sheldan.abstracto.core.models.database.ARole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoleDeletedModel {
    private ARole role;
}
