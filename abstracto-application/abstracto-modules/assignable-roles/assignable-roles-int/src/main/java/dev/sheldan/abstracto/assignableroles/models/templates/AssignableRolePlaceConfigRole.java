package dev.sheldan.abstracto.assignableroles.models.templates;


import dev.sheldan.abstracto.core.models.FullEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;

/**
 * Model used to display the configuration of an individual {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRole}
 * within an {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace place}
 */
@Getter
@Setter
@Builder
public class AssignableRolePlaceConfigRole {
    /**
     * The description used for the field for this {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRole role}
     */
    private String description;
    /**
     * The position of this {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRole role}
     */
    private Integer position;
    /**
     * Whether or not the field is displayed inline
     */
    private Boolean inline;
    /**
     * The {@link FullEmote emote} which is associated with this {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRole role}
     */
    private FullEmote emote;
    /**
     * The {@link dev.sheldan.abstracto.core.models.database.ARole} which is given/removed upon reacting with the emote
     */
    private Role awardedRole;
}
