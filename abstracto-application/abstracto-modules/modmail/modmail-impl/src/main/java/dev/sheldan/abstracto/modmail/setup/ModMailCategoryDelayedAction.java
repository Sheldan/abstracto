package dev.sheldan.abstracto.modmail.setup;

import dev.sheldan.abstracto.core.interactive.DelayedAction;
import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.modmail.service.ModMailThreadServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ModMailCategoryDelayedAction implements DelayedAction {


    @Autowired
    private ConfigService configService;

    @Override
    public void execute(DelayedActionConfig delayedActionConfig) {
        ModMailCategoryDelayedActionConfig concrete = (ModMailCategoryDelayedActionConfig) delayedActionConfig;
        configService.setConfigValue(ModMailThreadServiceBean.MODMAIL_CATEGORY, concrete.getServerId(), concrete.getValue());
    }

    @Override
    public boolean handles(DelayedActionConfig delayedActionConfig) {
        return delayedActionConfig instanceof ModMailCategoryDelayedActionConfig;
    }

}
