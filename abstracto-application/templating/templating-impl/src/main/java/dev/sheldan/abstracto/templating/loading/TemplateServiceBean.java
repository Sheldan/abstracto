package dev.sheldan.abstracto.templating.loading;

import dev.sheldan.abstracto.templating.TemplateDto;
import dev.sheldan.abstracto.templating.TemplateService;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;

@Slf4j
@Component
public class TemplateServiceBean implements TemplateService {

    @Autowired
    private TemplateRepository repository;

    @Autowired
    private Configuration configuration;

    @Override
    @Cacheable("template")
    public TemplateDto getTemplateByKey(String key) {
        return repository.getOne(key);
    }

    @Override
    public String renderTemplate(TemplateDto templateDto) {
        return null;
    }

    @Override
    public String renderTemplate(String key, HashMap<String, Object> parameters) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(key), parameters);
        } catch (IOException | TemplateException e) {
            log.warn("Failed to render template: {}", e.getMessage());
        }
        return "";
    }

    @Override
    public String renderTemplate(String key, Object model) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(key), model);
        } catch (IOException | TemplateException e) {
            log.warn("Failed to render template: {}", e.getMessage());
        }
        return "";
    }

    @Override
    public void createTemplate(String key, String content) {
        repository.save(TemplateDto.builder().key(key).content(content).lastModified(Instant.now()).build());
    }
}
