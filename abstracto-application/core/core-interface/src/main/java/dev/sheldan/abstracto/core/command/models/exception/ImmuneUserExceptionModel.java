package dev.sheldan.abstracto.core.command.models.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;

@Getter
@Setter
@Builder
public class ImmuneUserExceptionModel {
    private Role role;
}
