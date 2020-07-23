package dev.sheldan.abstracto.assignableroles.models.templates;


import dev.sheldan.abstracto.core.models.database.ARole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Role;

@Getter
@Setter
@Builder
public class AssignablePostConfigRole {
    private Emote emote;
    private String description;
    private Integer position;
    private Boolean inline;
    private ARole role;
    private Role awardedRole;
}
