package dev.sheldan.abstracto.modmail.validator;

import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.service.FeatureValidator;
import net.dv8tion.jda.api.entities.Guild;

public interface ModMailFeatureValidator extends FeatureValidator {
    void validateModMailCategory(FeatureValidationResult validationResult, Guild guild, Long modMailCategory);
}
