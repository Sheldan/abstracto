package dev.sheldan.abstracto.templating;

import java.util.HashMap;

public interface TemplateService {
    TemplateDto getTemplateByKey(String key);
    String renderTemplate(TemplateDto templateDto);
    String renderTemplate(String key, HashMap<String, Object> parameters);
    void createTemplate(String key, String content);
}
