package dev.sheldan.abstracto.core.templating.loading;

import dev.sheldan.abstracto.core.config.ServerContext;
import dev.sheldan.abstracto.core.templating.model.EffectiveTemplate;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.templating.service.management.EffectiveTemplateManagementService;
import freemarker.cache.TemplateLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Optional;

/**
 * Loads the the template from the database to be used by Freemarker. This bean is also used when the templates within
 * templates are used.
 */
@Component
public class DatabaseTemplateLoader implements TemplateLoader {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private EffectiveTemplateManagementService effectiveTemplateManagementService;

    @Autowired
    private ServerContext serverContext;

    /**
     * Loads the content of the template object
     * @param s The key of the template to load
     * @return The template loaded from the database
     */
    @Override
    public Object findTemplateSource(String s) throws IOException {
        Optional<EffectiveTemplate> templateByKey;
        if(s.contains("/")) {
            String[] parts = s.split("/");
            templateByKey = effectiveTemplateManagementService.getTemplateByKeyAndServer(parts[1], Long.parseLong(parts[0]));
        } else {
            templateByKey = effectiveTemplateManagementService.getTemplateByKey(s);
        }
        return templateByKey.orElseThrow(() -> new IOException(String.format("Failed to load template. %s", s)));
    }

    @Override
    public long getLastModified(Object o) {
        EffectiveTemplate casted = (EffectiveTemplate) o;
        Optional<EffectiveTemplate> templateByKey;
        if(serverContext.getServerId() != null) {
            templateByKey = effectiveTemplateManagementService.getTemplateByKeyAndServer(casted.getKey(), serverContext.getServerId());
        } else {
            templateByKey = effectiveTemplateManagementService.getTemplateByKey(casted.getKey());
        }
        return templateByKey.map(template -> template.getLastModified().getEpochSecond()).orElse(Long.MAX_VALUE);
    }

    /**
     * Retrieves the content of the template from the retrieved {@link EffectiveTemplate} object
     * @param o The retrieved {@link EffectiveTemplate} object from the database
     * @param s The encoding of the object
     * @return The content of the template as a String reader
     */
    @Override
    public Reader getReader(Object o, String s) throws IOException {
        return new StringReader(((EffectiveTemplate) o).getContent());
    }

    @Override
    public void closeTemplateSource(Object o) throws IOException {
        // do nothing for now
    }
}
