package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class CacheServiceBean {

    @Autowired
    private TemplateService templateService;

    public void clearCaches() {
        log.info("Clearing all caches.");
        templateService.clearCache();
    }
}
