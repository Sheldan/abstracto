package dev.sheldan.abstracto.core.templating.service.management;

import dev.sheldan.abstracto.core.templating.model.EffectiveTemplate;
import dev.sheldan.abstracto.core.templating.repository.EffectiveTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EffectiveTemplateManagementServiceBean implements EffectiveTemplateManagementService {

    @Autowired
    private EffectiveTemplateRepository effectiveTemplateRepository;

    @Override
    public Optional<EffectiveTemplate> getTemplateByKeyAndServer(String key, Long server) {
        return effectiveTemplateRepository.findByKeyAndServerId(key, server);
    }

    @Override
    public Optional<EffectiveTemplate> getTemplateByKey(String key) {
        return effectiveTemplateRepository.findByKey(key);
    }

}
