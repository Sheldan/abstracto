package dev.sheldan.abstracto.modmail.validator;

import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.service.FeatureValidator;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Validator service which validates, whether or not the configured mod mail category is actually a valid category.
 */
public interface ModMailFeatureValidator extends FeatureValidator {
    /**
     * Validates the category and checks if the given ID is a valid category in the given {@link Guild}
     * @param validationResult The object in which the result of the validation will be stored
     * @param guild The {@link Guild} to check for the category
     * @param modMailCategory The configured ID of the category
     */
    void validateModMailCategory(FeatureValidationResult validationResult, Guild guild, Long modMailCategory);
}
