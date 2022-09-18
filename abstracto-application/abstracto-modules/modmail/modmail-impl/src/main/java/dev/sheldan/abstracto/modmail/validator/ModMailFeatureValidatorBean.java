package dev.sheldan.abstracto.modmail.validator;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.FeatureValidatorService;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.modmail.model.template.ModMailCategoryValidationErrorModel;
import dev.sheldan.abstracto.modmail.service.ModMailThreadServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This component is used to validate whether or not the mod mail feature has a mod mail category configured, which points to
 * a category on the server it is configured for. This and other {@link dev.sheldan.abstracto.core.service.FeatureValidator}
 * are used to fully validate the mod mail feature.
 */
@Component
@Slf4j
public class ModMailFeatureValidatorBean implements ModMailFeatureValidator {

    @Autowired
    private GuildService guildService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private FeatureValidatorService featureValidatorService;

    /**
     * Checks if the mod mail category contains a value and whether or not this valid also points to a {@link Category}
     * @param featureConfig The instance of {@link FeatureConfig} of mod mail
     * @param server The {@link AServer} to check the config for
     * @param validationResult The current {@link FeatureValidationResult} used to accumulate the wrong values
     */
    @Override
    public void featureIsSetup(FeatureConfig featureConfig, AServer server, FeatureValidationResult validationResult) {
        Optional<Guild> guildById = guildService.getGuildByIdOptional(server.getId());
        if(guildById.isPresent()) {
            Guild guild = guildById.get();
            boolean checkSucceeded = featureValidatorService.checkSystemConfig(ModMailThreadServiceBean.MODMAIL_CATEGORY, server, validationResult);
            log.debug("Validating the modmail category for server {}.", server.getId());
            if(checkSucceeded) {
                log.debug("Modmail category has been set for server {}. Lets see if the category exists.", server.getId());
                Long modMailCategory = configService.getLongValue(ModMailThreadServiceBean.MODMAIL_CATEGORY, server.getId());
                validateModMailCategory(validationResult, guild, modMailCategory);
            }
        }
    }

    /**
     * Validates the category and checks if the given ID is a valid category in the given {@link Guild}
     * @param validationResult The object in which the result of the validation will be stored
     * @param guild The {@link Guild} to check for the category
     * @param modMailCategory The configured ID of the category
     */
    public void validateModMailCategory(FeatureValidationResult validationResult, Guild guild, Long modMailCategory) {
        Category categoryById = guild.getCategoryById(modMailCategory);
        if(categoryById == null) {
            validationResult.setValidationResult(false);
            ModMailCategoryValidationErrorModel newError = ModMailCategoryValidationErrorModel
                    .builder()
                    .currentCategoryId(modMailCategory)
                    .build();
            validationResult.getValidationErrorModels().add(newError);
        }
    }
}
