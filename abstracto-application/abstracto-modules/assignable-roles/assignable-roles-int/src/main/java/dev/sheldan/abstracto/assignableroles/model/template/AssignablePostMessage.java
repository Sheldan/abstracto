package dev.sheldan.abstracto.assignableroles.model.template;

import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model used to render the {@link AssignableRolePlace place}
 */
@Getter
@Setter
@Builder
public class AssignablePostMessage {
    private Long placeId;
    private String placeDescription;
    /**
     * The awarded {@link AssignablePostRole roles} for this {@link AssignableRolePlace place}
     */
    private List<AssignablePostRole> roles;
    /**
     * The highest number of the position within the {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRole} of the
     * {@link AssignableRolePlace place}
     */
    private Integer maxPosition;
    @Builder.Default
    private Map<String, Long> componentIdToRole = new HashMap<>();
}
