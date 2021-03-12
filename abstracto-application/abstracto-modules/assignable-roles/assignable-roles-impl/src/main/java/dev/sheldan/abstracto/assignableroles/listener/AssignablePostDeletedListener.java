package dev.sheldan.abstracto.assignableroles.listener;

import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlacePost;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlacePostManagementService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageDeletedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class AssignablePostDeletedListener implements AsyncMessageDeletedListener {

    @Autowired
    private AssignableRolePlacePostManagementService service;

    /**
     * This method deletes one individual {@link AssignableRolePlacePost post}, because its message has been deleted
     * @param messageBefore The {@link CachedMessage message} which was deleted
     */
    @Override
    public void execute(CachedMessage messageBefore) {
        Optional<AssignableRolePlacePost> messageOptional = service.findByMessageIdOptional(messageBefore.getMessageId());
        messageOptional.ifPresent(post -> {
            AssignableRolePlace assignablePlace = post.getAssignablePlace();
            log.info("Post {} has been deleted in server {} in channel {}, we are removing a post from place {}.", post.getId(), messageBefore.getServerId(), messageBefore.getChannelId(), assignablePlace.getKey());
            post.getAssignableRoles().forEach(assignableRole -> assignableRole.setAssignableRolePlacePost(null));
            assignablePlace.getMessagePosts().remove(post);
        });
    }

    @Override
    public FeatureDefinition getFeature() {
        return AssignableRoleFeatureDefinition.ASSIGNABLE_ROLES;
    }

}
