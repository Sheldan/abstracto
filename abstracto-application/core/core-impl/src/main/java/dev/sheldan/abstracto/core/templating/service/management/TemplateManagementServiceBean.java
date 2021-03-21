package dev.sheldan.abstracto.core.templating.service.management;

import dev.sheldan.abstracto.core.templating.model.database.Template;
import dev.sheldan.abstracto.core.templating.repository.TemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/**
 * ManagementService bean used to retrieve the templates by key from the database.
 * This class uses the {@link TemplateRepository} bean to load the {@link Template} objects.
 */
@Component
@Slf4j
public class TemplateManagementServiceBean implements TemplateManagementService {

    @Autowired
    private TemplateRepository repository;

    /**
     * Returns the template from the database by key
     * @param key They template key to search for
     * @return An {@link Optional} containing the {@link Template} if any was found.
     */
    @Override
    public Optional<Template> getTemplateByKey(String key) {
        return repository.findById(key);
    }

    /**
     * Returns whether or not the template identified by the key exists in the database
     * @param key They key of the template to search for
     * @return Whether or not the template exists in the database
     */
    @Override
    public boolean templateExists(String key) {
        return repository.existsById(key);
    }

    /**
     * Creates a {@link Template} object and stores it in the database. Returns the newly created object.
     * @param key They key of the template to create.
     * @param content The content the template should have
     * @return The {@link Template} which was created
     */
    @Override
    public Template createTemplate(String key, String content) {
        Template template = Template.builder().key(key).content(content).lastModified(Instant.now()).build();
        return repository.save(template);
    }

}
