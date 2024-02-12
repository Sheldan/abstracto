package dev.sheldan.abstracto.stickyroles.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncLeaveListener;
import dev.sheldan.abstracto.core.models.listener.MemberLeaveModel;
import dev.sheldan.abstracto.stickyroles.config.StickyRolesFeatureDefinition;
import dev.sheldan.abstracto.stickyroles.service.StickyRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StickyRolesLeaveListener implements AsyncLeaveListener {

    @Autowired
    private StickyRoleService stickyRoleService;

    @Override
    public DefaultListenerResult execute(MemberLeaveModel model) {
        if(model.getMember() != null) {
            stickyRoleService.handleLeave(model.getMember());
            return DefaultListenerResult.PROCESSED;
        } else {
            log.warn("Member object was not found for storing sticky roles for user {} in server {}.", model.getLeavingUser().getUserId(), model.getServerId());
        }

        return DefaultListenerResult.IGNORED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return StickyRolesFeatureDefinition.STICKY_ROLES;
    }
}
