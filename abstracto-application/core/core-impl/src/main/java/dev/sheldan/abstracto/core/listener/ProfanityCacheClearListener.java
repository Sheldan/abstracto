package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.listener.async.entity.AsyncCacheClearingListener;
import dev.sheldan.abstracto.core.models.listener.VoidListenerModel;
import dev.sheldan.abstracto.core.service.ProfanityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProfanityCacheClearListener implements AsyncCacheClearingListener {

    @Autowired
    private ProfanityService profanityService;

    @Override
    public DefaultListenerResult execute(VoidListenerModel model) {
        log.info("Reloading regexes, because cache cleared.");
        profanityService.reloadRegex();
        return DefaultListenerResult.PROCESSED;
    }
}
