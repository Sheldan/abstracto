package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PostTargetDelayedAction implements DelayedAction {

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Override
    public void execute(DelayedActionConfig delayedActionConfig) {
        PostTargetDelayedActionConfig castedConfig = (PostTargetDelayedActionConfig) delayedActionConfig;
        log.trace("Executing post target delayed step to set post target {} to channel {} in server {}.", castedConfig.getPostTargetKey(), castedConfig.getChannelId(), castedConfig.getServerId());
        postTargetManagement.createOrUpdate(castedConfig.getPostTargetKey(), castedConfig.getServerId(), castedConfig.getChannelId());
    }

    @Override
    public boolean handles(DelayedActionConfig delayedActionConfig) {
        return delayedActionConfig instanceof PostTargetDelayedActionConfig;
    }
}
