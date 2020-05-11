package dev.sheldan.abstracto.templating.service.management;

import dev.sheldan.abstracto.templating.repository.TemplateRepository;
import dev.sheldan.abstracto.templating.model.database.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * ManagementService bean used to retrieve the templates by key from the database.
 * This class uses the {@link TemplateRepository} bean to laod the {@link Template} objects.
 */
@Component
@Slf4j
public class TemplateManagementServiceBean implements TemplateManagementService {

    @Autowired
    private TemplateRepository repository;

    @Override
    public Template getTemplateByKey(String key) {
        return repository.getOne(key);
    }

    @Override
    public boolean templateExists(String key) {
        return repository.existsById(key);
    }

    @Override
    public Template createTemplate(String key, String content) {
        Template build = Template.builder().key(key).content(content).lastModified(Instant.now()).build();
        repository.save(build);
        return build;
    }

}
