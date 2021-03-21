package dev.sheldan.abstracto.assignableroles.listener;

import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlacePost;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlacePostManagementService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageDeletedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.MessageDeletedModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class AssignablePostDeletedListener implements AsyncMessageDeletedListener {

    @Autowired
    private AssignableRolePlacePostManagementService service;

    @Override
    public FeatureDefinition getFeature() {
        return AssignableRoleFeatureDefinition.ASSIGNABLE_ROLES;
    }

    /**
     * This method deletes one individual {@link AssignableRolePlacePost post}, because its message has been deleted
     * @param model The {@link MessageDeletedModel message} containing the {@link CachedMessage cachedMessage} which was deleted
     */
    @Override
    public DefaultListenerResult execute(MessageDeletedModel model) {
        Optional<AssignableRolePlacePost> messageOptional = service.findByMessageIdOptional(model.getCachedMessage().getMessageId());
        messageOptional.ifPresent(post -> {
            AssignableRolePlace assignablePlace = post.getAssignablePlace();
            log.info("Post {} has been deleted in server {} in channel {}, we are removing a post from place {}.",
                    post.getId(), model.getServerId(), model.getCachedMessage().getChannelId(), assignablePlace.getKey());
            post.getAssignableRoles().forEach(assignableRole -> assignableRole.setAssignableRolePlacePost(null));
            assignablePlace.getMessagePosts().remove(post);
        });

        return DefaultListenerResult.PROCESSED;
    }
}
