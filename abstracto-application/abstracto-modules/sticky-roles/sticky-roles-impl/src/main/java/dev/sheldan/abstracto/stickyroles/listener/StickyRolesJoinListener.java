package dev.sheldan.abstracto.stickyroles.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncUpdatePendingListener;
import dev.sheldan.abstracto.core.models.listener.MemberUpdatePendingModel;
import dev.sheldan.abstracto.stickyroles.config.StickyRolesFeatureDefinition;
import dev.sheldan.abstracto.stickyroles.service.StickyRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StickyRolesJoinListener implements AsyncUpdatePendingListener {

    @Autowired
    private StickyRoleService stickyRoleService;

    @Override
    public DefaultListenerResult execute(MemberUpdatePendingModel model) {
        stickyRoleService.handleJoin(model.getMember());
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return StickyRolesFeatureDefinition.STICKY_ROLES;
    }
}
