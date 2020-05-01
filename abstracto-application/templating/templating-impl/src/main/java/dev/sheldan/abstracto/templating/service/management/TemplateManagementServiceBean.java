package dev.sheldan.abstracto.templating.service.management;

import dev.sheldan.abstracto.templating.loading.TemplateRepository;
import dev.sheldan.abstracto.templating.model.database.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

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
        return getTemplateByKey(key) != null;
    }

    @Override
    public Template createTemplate(String key, String content) {
        Template build = Template.builder().key(key).content(content).lastModified(Instant.now()).build();
        repository.save(build);
        return build;
    }

}
