package dev.sheldan.abstracto.core.templating.listener;

import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.entity.AsyncCacheClearingListener;
import dev.sheldan.abstracto.core.models.listener.VoidListenerModel;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TemplateCacheListener implements AsyncCacheClearingListener {

    @Autowired
    private TemplateService templateService;

    @Override
    public DefaultListenerResult execute(VoidListenerModel model) {
        log.info("Clearing freemarker caches.");
        templateService.clearCache();
        return DefaultListenerResult.PROCESSED;
    }
}
