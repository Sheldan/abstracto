package dev.sheldan.abstracto.templating.service.management;

import dev.sheldan.abstracto.templating.model.database.Template;

public interface TemplateManagementService {
    Template getTemplateByKey(String key);
    boolean templateExists(String key);
    void createTemplate(String key, String content);
}
