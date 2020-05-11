package dev.sheldan.abstracto.templating.config;

import dev.sheldan.abstracto.templating.service.management.TemplateManagementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Loads the available templates from the class path and uploads them to the database, overriding existing templates in the process.
 * This will load all *.ftl files at any level within a folder named 'templates' in the resources folder.
 */
@Component
@Slf4j
public class TemplateSeedDataLoader {

    @Value("classpath*:**/templates/**/*.ftl")
    private Resource[] resources;

    @Autowired
    private TemplateManagementService service;

    /**
     * Is executed when the spring context is started, this will load all templates from the class path and
     * store them in the database overriding the existing ones in the process.
     */
    @EventListener
    public void handleContextRefreshEvent(ContextRefreshedEvent ctxStartEvt) {
        log.info("Updating templates.");
        List<Resource> templatesToLoad = Arrays.asList(resources);
        templatesToLoad.forEach(resource -> {
            try {
                String templateKey = FilenameUtils.getBaseName(resource.getFilename());
                String templateContent = IOUtils.toString(resource.getURI(), Charset.defaultCharset());
                log.trace("Creating template {}", templateKey);
                service.createTemplate(templateKey, templateContent);
            } catch (IOException e) {
                log.error("Failed to upload template", e);
            }
        });
    }
}
