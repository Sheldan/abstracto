package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostTargetDelayedAction implements DelayedAction {

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Override
    public void execute(DelayedActionConfig delayedActionConfig) {
        PostTargetDelayedActionConfig concrete = (PostTargetDelayedActionConfig) delayedActionConfig;
        postTargetManagement.createOrUpdate(concrete.getPostTargetKey(), concrete.getServerId(), concrete.getChannelId());
    }

    @Override
    public boolean handles(DelayedActionConfig delayedActionConfig) {
        return delayedActionConfig instanceof PostTargetDelayedActionConfig;
    }
}
