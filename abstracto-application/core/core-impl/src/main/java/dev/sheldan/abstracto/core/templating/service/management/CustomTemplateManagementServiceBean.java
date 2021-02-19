package dev.sheldan.abstracto.core.templating.service.management;

import dev.sheldan.abstracto.core.exception.TemplateNotFoundException;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.database.CustomTemplate;
import dev.sheldan.abstracto.core.templating.repository.CustomTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class CustomTemplateManagementServiceBean implements CustomTemplateManagementService {

    @Autowired
    private CustomTemplateRepository customTemplateRepository;

    @Autowired
    private TemplateManagementService templateManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CustomTemplate createOrUpdateCustomTemplate(String templateKey, String templateContent, Long serverId) {
        if(!templateManagementService.templateExists(templateKey)) {
            throw new TemplateNotFoundException(templateKey);
        }
        Optional<CustomTemplate> customTemplateOptional = getCustomTemplate(templateKey, serverId);
        customTemplateOptional.ifPresent(customTemplate -> customTemplate.setContent(templateContent));
        return customTemplateOptional.orElseGet(() -> createCustomTemplate(templateKey, templateContent, serverId));
    }

    @Override
    public CustomTemplate createCustomTemplate(String templateKey, String templateContent, Long serverId) {
        CustomTemplate template = CustomTemplate
                .builder()
                .server(serverManagementService.loadServer(serverId))
                .content(templateContent)
                .key(templateKey)
                .build();
        customTemplateRepository.save(template);
        return template;
    }

    @Override
    public Optional<CustomTemplate> getCustomTemplate(String templateKey, Long serverId) {
        return customTemplateRepository.findByKeyAndServerId(templateKey, serverId);
    }

    @Override
    public boolean doesCustomTemplateExist(String templateKey, Long serverId) {
        return getCustomTemplate(templateKey, serverId).isPresent();
    }

    @Override
    public void deleteCustomTemplateByKey(String templateKey, Long serverId) {
        Optional<CustomTemplate> customTemplateOptional = getCustomTemplate(templateKey, serverId);
        customTemplateOptional.ifPresent(this::deleteCustomTemplate);
    }

    @Override
    public void deleteCustomTemplate(CustomTemplate customTemplate) {
        customTemplateRepository.delete(customTemplate);
    }
}
