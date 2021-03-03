package dev.sheldan.abstracto.experience.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Container object to store the experience in runtime and group it together. This basically is just a list of users who were tracked by experience.
 * The actual calculation of the appropriate experience amount is done later.
 */
@Getter
@Setter
@Builder
public class ServerExperience {
    /**
     * The ID of the {@link dev.sheldan.abstracto.core.models.database.AServer} for which this experience were collected
     */
    private Long serverId;
    /**
     * A list of IDs of the {@link dev.sheldan.abstracto.core.models.database.AUserInAServer} which should be given experience
     */
    @Builder.Default
    private List<Long> userInServerIds = new ArrayList<>();
}
