package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.ADefaultConfig;
import dev.sheldan.abstracto.core.repository.DefaultConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultConfigManagementServiceBean implements DefaultConfigManagementService {

    @Autowired
    private DefaultConfigRepository defaultConfigRepository;

    @Override
    public void createDefaultConfig(String key, String value) {
        ADefaultConfig build;
        if(defaultConfigRepository.existsByName(key)) {
            build = defaultConfigRepository.findByName(key);
            build.setStringValue(value);
        } else {
            build = ADefaultConfig
                    .builder()
                    .name(key)
                    .stringValue(value)
                    .build();
            log.trace("Creating default config entry with type string for key {}.", key);
        }
        defaultConfigRepository.save(build);
    }

    @Override
    public void createDefaultConfig(String key, Long value) {
        ADefaultConfig build;
        if(defaultConfigRepository.existsByName(key)) {
            build = defaultConfigRepository.findByName(key);
            build.setLongValue(value);
        } else {
            build = ADefaultConfig
                    .builder()
                    .name(key)
                    .longValue(value)
                    .build();
            log.trace("Creating default config entry with type long for key {}.", key);
        }
        defaultConfigRepository.save(build);
    }

    @Override
    public void createDefaultConfig(String key, Double value) {
        ADefaultConfig build;
        if(defaultConfigRepository.existsByName(key)) {
            build = defaultConfigRepository.findByName(key);
            build.setDoubleValue(value);
        } else {
            build = ADefaultConfig
                    .builder()
                    .name(key)
                    .doubleValue(value)
                    .build();
            log.trace("Creating default config entry with type double for key {}.", key);
        }
        defaultConfigRepository.save(build);
    }

    @Override
    public ADefaultConfig getDefaultConfig(String key) {
        return defaultConfigRepository.findByName(key);
    }
}
