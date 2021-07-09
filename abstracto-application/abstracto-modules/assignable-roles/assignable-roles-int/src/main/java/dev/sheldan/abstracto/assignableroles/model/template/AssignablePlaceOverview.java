package dev.sheldan.abstracto.assignableroles.model.template;

import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Model used to render the overview over all {@link AssignableRolePlace places}
 */
@Getter
@Setter
@Builder
public class AssignablePlaceOverview {
    /**
     * The {@link AssignableRolePlace places} in the server to display
     */
    @Builder.Default
    private List<AssignableRolePlaceConfig> places = new ArrayList<>();
}
