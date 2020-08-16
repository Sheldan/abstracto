package dev.sheldan.abstracto.assignableroles.models.templates;


import dev.sheldan.abstracto.core.models.FullEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;

@Getter
@Setter
@Builder
public class AssignablePostConfigRole {
    private String description;
    private Integer position;
    private Boolean inline;
    private FullEmote emote;
    private Role awardedRole;
}
