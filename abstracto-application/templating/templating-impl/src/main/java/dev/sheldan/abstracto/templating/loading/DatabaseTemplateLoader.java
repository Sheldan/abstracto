package dev.sheldan.abstracto.templating.loading;

import dev.sheldan.abstracto.templating.TemplateDto;
import dev.sheldan.abstracto.templating.TemplateService;
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

    @Override
    public Object findTemplateSource(String s) throws IOException {
        return templateService.getTemplateByKey(s);
    }

    @Override
    public long getLastModified(Object o) {
        TemplateDto casted = (TemplateDto) o;
        TemplateDto templateDtoByKey = templateService.getTemplateByKey(casted.getKey());
        if(templateDtoByKey != null){
            return templateDtoByKey.getLastModified().getEpochSecond();
        } else {
            return Long.MAX_VALUE;
        }
    }

    @Override
    public Reader getReader(Object o, String s) throws IOException {
        return new StringReader(((TemplateDto) o).getContent());
    }

    @Override
    public void closeTemplateSource(Object o) throws IOException {

    }
}
