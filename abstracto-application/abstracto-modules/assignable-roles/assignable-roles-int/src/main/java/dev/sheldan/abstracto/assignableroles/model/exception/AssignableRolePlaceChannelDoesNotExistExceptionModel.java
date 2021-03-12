package dev.sheldan.abstracto.assignableroles.model.exception;

import dev.sheldan.abstracto.assignableroles.exception.AssignableRolePlaceChannelDoesNotExistException;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Contains the model for {@link AssignableRolePlaceChannelDoesNotExistException}
 */
@Getter
@Builder
public class AssignableRolePlaceChannelDoesNotExistExceptionModel implements Serializable {
    /**
     * The ID of the {@link dev.sheldan.abstracto.core.models.database.AChannel channel} which does not exist in the {@link net.dv8tion.jda.api.entities.Guild}
     */
    private final Long channelId;
    /**
     * The key of the {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace place} for which
     * the channel does not exist anymore in the {@link net.dv8tion.jda.api.entities.Guild}
     */
    private final String placeName;
}
