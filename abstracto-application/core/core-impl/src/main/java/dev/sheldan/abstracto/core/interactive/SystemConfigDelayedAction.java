package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SystemConfigDelayedAction implements DelayedAction {


    @Autowired
    private ConfigService configService;

    @Override
    public void execute(DelayedActionConfig delayedActionConfig) {
        SystemConfigDelayedActionConfig concrete = (SystemConfigDelayedActionConfig) delayedActionConfig;
        log.debug("Executing delayed system config action for key {} in server {}.", concrete.getConfigKey(), concrete.getServerId());
        configService.setOrCreateConfigValue(concrete.getServerId(), concrete.getConfigKey(), concrete.getValue());
    }

    @Override
    public boolean handles(DelayedActionConfig delayedActionConfig) {
        return delayedActionConfig instanceof SystemConfigDelayedActionConfig;
    }

}
