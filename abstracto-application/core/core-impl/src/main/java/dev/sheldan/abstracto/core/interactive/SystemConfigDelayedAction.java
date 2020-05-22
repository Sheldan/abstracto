package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SystemConfigDelayedAction implements DelayedAction {


    @Autowired
    private ConfigService configService;

    @Override
    public void execute(DelayedActionConfig delayedActionConfig) {
        SystemConfigDelayedActionConfig concrete = (SystemConfigDelayedActionConfig) delayedActionConfig;
        configService.setConfigValue(concrete.getConfigKey(), concrete.getServerId(), concrete.getValue());
    }

    @Override
    public boolean handles(DelayedActionConfig delayedActionConfig) {
        return delayedActionConfig instanceof SystemConfigDelayedActionConfig;
    }

}
