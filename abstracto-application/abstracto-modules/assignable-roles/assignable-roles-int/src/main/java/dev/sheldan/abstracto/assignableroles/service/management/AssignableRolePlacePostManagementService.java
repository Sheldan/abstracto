package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlacePost;

import java.util.Optional;

public interface AssignableRolePlacePostManagementService {
    Optional<AssignableRolePlacePost> findByMessageIdOptional(Long messageId);
    AssignableRolePlacePost findByMessageId(Long messageId);
    AssignableRolePlacePost createAssignableRolePlacePost(AssignableRolePlace updatedPlace, Long messageId);
}
