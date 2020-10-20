package dev.sheldan.abstracto.assignableroles.listener;

import dev.sheldan.abstracto.assignableroles.config.features.AssignableRoleFeature;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlacePost;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlacePostManagementService;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.MessageDeletedListener;
import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class AssignablePostDeletedListener implements MessageDeletedListener {

    @Autowired
    private AssignableRolePlacePostManagementService service;

    @Override
    public void execute(CachedMessage messageBefore, AServerAChannelAUser authorUser, GuildChannelMember authorMember) {
        Optional<AssignableRolePlacePost> messageOptional = service.findByMessageIdOptional(messageBefore.getMessageId());
        messageOptional.ifPresent(post -> {
            AssignableRolePlace assignablePlace = post.getAssignablePlace();
            log.info("Post {} has been deleted in server {} in channel {}, we are removing a post from place {}.", post.getId(), messageBefore.getServerId(), messageBefore.getChannelId(), assignablePlace.getKey());
            post.getAssignableRoles().forEach(assignableRole -> assignableRole.setAssignableRolePlacePost(null));
            assignablePlace.getMessagePosts().remove(post);
        });
    }

    @Override
    public FeatureEnum getFeature() {
        return AssignableRoleFeature.ASSIGNABLE_ROLES;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.LOW;
    }
}
