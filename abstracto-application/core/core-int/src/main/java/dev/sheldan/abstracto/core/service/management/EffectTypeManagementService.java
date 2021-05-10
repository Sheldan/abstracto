package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.EffectType;

import java.util.List;
import java.util.Optional;

public interface EffectTypeManagementService {
    Optional<EffectType> loadEffectTypeByKeyOptional(String effectType);
    EffectType loadEffectTypeByKey(String effectType);
    List<EffectType> getAllEffects();
}
