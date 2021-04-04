package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.listener.async.entity.AsyncCacheClearingListener;
import dev.sheldan.abstracto.core.models.listener.VoidListenerModel;
import dev.sheldan.abstracto.core.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClearMessageCacheListener implements AsyncCacheClearingListener {

    @Autowired
    private MessageCache messageCache;

    @Override
    public DefaultListenerResult execute(VoidListenerModel model) {
        log.debug("Executing clear message cache listener.");
        messageCache.clearCache();
        return DefaultListenerResult.PROCESSED;
    }
}
