package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.moderation.config.features.WarningDecayFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ModerationDefaultConfigListener {

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Value("${abstracto.warnings.warnDecay.days}")
    private Long decayDays;

    @EventListener
    @Transactional
    public void handleContextRefreshEvent(ContextRefreshedEvent ctxStartEvt) {
        defaultConfigManagementService.createDefaultConfig(WarningDecayFeature.DECAY_DAYS_KEY, decayDays);
    }
}
