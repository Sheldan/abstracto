package dev.sheldan.abstracto.templating.loading;

import dev.sheldan.abstracto.templating.model.database.Template;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.templating.service.management.TemplateManagementService;
import freemarker.cache.TemplateLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Loads the the template from the database to be used by Freemarker. This bean is also used when the templates within
 * templates are used.
 */
@Component
public class DatabaseTemplateLoader implements TemplateLoader {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TemplateManagementService templateManagementService;

    /**
     * Loads the content of the template object
     * @param s The key of the template to load
     * @return The template loaded from the database
     */
    @Override
    public Object findTemplateSource(String s) throws IOException {
        return templateManagementService.getTemplateByKey(s);
    }

    @Override
    public long getLastModified(Object o) {
        Template casted = (Template) o;
        Template templateByKey = templateManagementService.getTemplateByKey(casted.getKey());
        if(templateByKey != null){
            return templateByKey.getLastModified().getEpochSecond();
        } else {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Retrieves the content of the template from the retrieved {@link Template} object
     * @param o The retrieved {@link Template} object from the database
     * @param s The encoding of the object
     * @return The content of the template as a String reader
     */
    @Override
    public Reader getReader(Object o, String s) throws IOException {
        return new StringReader(((Template) o).getContent());
    }

    @Override
    public void closeTemplateSource(Object o) throws IOException {

    }
}
