package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.database.ARole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;

@Getter
@Setter
@Builder
public class FullRole {
    private ARole role;
    private Role serverRole;
}
