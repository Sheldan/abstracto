package dev.sheldan.abstracto.core.templating.service.management;

import dev.sheldan.abstracto.core.templating.model.database.CustomTemplate;

import java.util.Optional;

public interface CustomTemplateManagementService {
    CustomTemplate createOrUpdateCustomTemplate(String templateKey, String templateContent, Long serverId);
    CustomTemplate createCustomTemplate(String templateKey, String templateContent, Long serverId);
    Optional<CustomTemplate> getCustomTemplate(String templateKey, Long serverId);
    boolean doesCustomTemplateExist(String templateKey, Long serverId);
    void deleteCustomTemplateByKey(String templateKey, Long serverId);
    void deleteCustomTemplate(CustomTemplate customTemplate);
}
