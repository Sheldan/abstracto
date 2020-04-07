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

@Component
public class DatabaseTemplateLoader implements TemplateLoader {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TemplateManagementService templateManagementService;

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

    @Override
    public Reader getReader(Object o, String s) throws IOException {
        return new StringReader(((Template) o).getContent());
    }

    @Override
    public void closeTemplateSource(Object o) throws IOException {

    }
}
