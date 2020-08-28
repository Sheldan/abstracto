package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.database.ARole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class FullRole implements Serializable {
    private ARole role;
    private transient Role serverRole;

    public String getRoleRepr() {
        if(serverRole != null) {
            return serverRole.getAsMention();
        } else {
            return role.getId().toString();
        }
    }
}
