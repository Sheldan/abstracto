package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.EffectTypeNotFoundException;
import dev.sheldan.abstracto.core.models.database.EffectType;
import dev.sheldan.abstracto.core.repository.EffectTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EffectTypeManagementServiceBean implements EffectTypeManagementService {

    @Autowired
    private EffectTypeRepository effectTypeRepository;

    @Override
    public Optional<EffectType> loadEffectTypeByKeyOptional(String effectType) {
        return effectTypeRepository.findByEffectTypeKey(effectType);
    }

    @Override
    public EffectType loadEffectTypeByKey(String effectType) {
        return loadEffectTypeByKeyOptional(effectType).orElseThrow(EffectTypeNotFoundException::new);
    }

    @Override
    public List<EffectType> getAllEffects() {
        return effectTypeRepository.findAll();
    }
}
