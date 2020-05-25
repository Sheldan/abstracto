package dev.sheldan.abstracto.modmail.validator;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.FeatureValidatorService;
import dev.sheldan.abstracto.modmail.models.template.ModMailCategoryValidationError;
import dev.sheldan.abstracto.modmail.service.ModMailThreadServiceBean;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ModMailFeatureValidatorBean implements ModMailFeatureValidator {

    @Autowired
    private BotService botService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private FeatureValidatorService featureValidatorService;

    @Override
    public void featureIsSetup(FeatureConfig featureConfig, AServer server, FeatureValidationResult validationResult) {
        Optional<Guild> guildById = botService.getGuildById(server.getId());
        if(guildById.isPresent()) {
            Guild guild = guildById.get();
            boolean checkSucceeded = featureValidatorService.checkSystemConfig(ModMailThreadServiceBean.MODMAIL_CATEGORY, server, validationResult);
            if(checkSucceeded) {
                Long modMailCategory = configService.getLongValue(ModMailThreadServiceBean.MODMAIL_CATEGORY, server.getId());
                validateModMailCategory(validationResult, guild, modMailCategory);
            }
        }
    }

    public void validateModMailCategory(FeatureValidationResult validationResult, Guild guild, Long modMailCategory) {
        Category categoryById = guild.getCategoryById(modMailCategory);
        if(categoryById == null) {
            validationResult.setValidationResult(false);
            ModMailCategoryValidationError newError = ModMailCategoryValidationError
                    .builder()
                    .currentCategoryId(modMailCategory)
                    .build();
            validationResult.getValidationErrors().add(newError);
        }
    }
}
