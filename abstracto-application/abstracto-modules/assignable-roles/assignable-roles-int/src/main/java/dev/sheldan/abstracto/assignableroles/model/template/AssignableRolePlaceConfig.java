package dev.sheldan.abstracto.assignableroles.model.template;

import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.core.models.template.display.ChannelDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * This model is used as a container to display the configuration of an {@link AssignableRolePlace place}
 */
@Getter
@Setter
@Builder
public class AssignableRolePlaceConfig {
    private String placeName;
    private String placeText;
    private ChannelDisplay channelDisplay;
    private Boolean uniqueRoles;
    /**
     * The {@link AssignableRolePlaceConfig roles} which are contained in this {@link AssignableRolePlace}
     */
    private List<AssignableRolePlaceConfigRole> roles;
}
