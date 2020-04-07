package dev.sheldan.abstracto.templating.service.management;

import dev.sheldan.abstracto.templating.loading.TemplateRepository;
import dev.sheldan.abstracto.templating.model.database.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TemplateManagementServiceBean implements TemplateManagementService {

    @Autowired
    private TemplateRepository repository;

    @Override
    public Template getTemplateByKey(String key) {
        return repository.getOne(key);
    }

    @Override
    public boolean templateExists(String key) {
        return getTemplateByKey(key) != null;
    }

    @Override
    public void createTemplate(String key, String content) {
        repository.save(Template.builder().key(key).content(content).lastModified(Instant.now()).build());
    }
}
