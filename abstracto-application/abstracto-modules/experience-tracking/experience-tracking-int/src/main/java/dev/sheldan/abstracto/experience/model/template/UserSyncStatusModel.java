package dev.sheldan.abstracto.experience.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Object containing the status update information when the user executes the
 * command responsible for synchronizing the users with their experience roles. This is a very small
 * object as it only contains the current count and the total amount.
 */
@Getter
@Setter
@Builder
public class UserSyncStatusModel {
    /**
     * The amount of users which already have been processed.
     */
    private Integer currentCount;
    /**
     * The total amount of users for which the experience roles are being synchronized.
     */
    private Integer totalUserCount;
}
