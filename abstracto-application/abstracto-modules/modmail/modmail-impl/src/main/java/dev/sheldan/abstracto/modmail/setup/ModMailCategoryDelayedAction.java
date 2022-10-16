package dev.sheldan.abstracto.modmail.setup;

import dev.sheldan.abstracto.core.interactive.DelayedAction;
import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.modmail.service.ModMailThreadServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This delayed action is responsible for setting the system configuration of the mod mail category for a given server
 */
@Component
@Slf4j
public class ModMailCategoryDelayedAction implements DelayedAction {

    @Autowired
    private ConfigService configService;

    /**
     * Sets the config key of MODMAIL_CATEGORY to the given category ID contained in the {@link DelayedActionConfig}
     * @param delayedActionConfig An instance of {@link ModMailCategoryDelayedActionConfig} containing the ID
     *                            of the category and the ID of the
     *                            {@link net.dv8tion.jda.api.entities.Guild} to change
     */
    @Override
    public void execute(DelayedActionConfig delayedActionConfig) {
        ModMailCategoryDelayedActionConfig concrete = (ModMailCategoryDelayedActionConfig) delayedActionConfig;
        log.info("Executing delayed action for configuration the mdomail category to {} in server {}.", concrete.getCategoryId(), concrete.getServerId());
        configService.setOrCreateConfigValue(ModMailThreadServiceBean.MODMAIL_CATEGORY, concrete.getServerId(), concrete.getCategoryId().toString());
    }

    /**
     * This delayed action only reacts to delayed action configurations of typ {@link ModMailCategoryDelayedActionConfig}.
     * As this the instance bound to this {@link DelayedAction}
     * @param delayedActionConfig An instance of check whether or not this {@link DelayedAction} should be executed for this
     *                            {@link DelayedActionConfig}
     * @return Whether or not the passed {@link DelayedActionConfig} is going to be handled by this class.
     */
    @Override
    public boolean handles(DelayedActionConfig delayedActionConfig) {
        return delayedActionConfig instanceof ModMailCategoryDelayedActionConfig;
    }

}
