package dev.sheldan.abstracto.modmail.validator;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailMode;
import dev.sheldan.abstracto.modmail.config.ModMailPostTargets;
import dev.sheldan.abstracto.modmail.model.template.ModMailCategoryValidationErrorModel;
import dev.sheldan.abstracto.modmail.model.template.ModMailThreadContainerValidationErrorModel;
import dev.sheldan.abstracto.modmail.service.ModMailThreadServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This component is used to validate whether the mod mail feature has a mod mail category configured, which points to
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

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private PostTargetService postTargetService;

    /**
     * Checks if the mod mail category contains a value and whether this valid also points to a {@link Category}.
     * Additionally, if the thread container feature mode is already enabled. This will check if the current posttarget
     * for threads can hold threads.
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
            if (featureModeService.featureModeActive(ModMailFeatureDefinition.MOD_MAIL, server.getId(), ModMailMode.THREAD_CONTAINER)) {
                Optional<GuildMessageChannel> modmailContainerOptional = postTargetService.getPostTargetChannel(ModMailPostTargets.MOD_MAIL_CONTAINER, server.getId());
                if(modmailContainerOptional.isEmpty()) {
                    ModMailThreadContainerValidationErrorModel newError = ModMailThreadContainerValidationErrorModel
                            .builder()
                            .currentChannelId(null)
                            .build();
                    validationResult.getValidationErrorModels().add(newError);
                    validationResult.setValidationResult(false);
                } else {
                    GuildMessageChannel threadContainer = modmailContainerOptional.get();
                    if(!(threadContainer instanceof IThreadContainer)) {
                        validationResult.setValidationResult(false);
                        ModMailThreadContainerValidationErrorModel newError = ModMailThreadContainerValidationErrorModel
                                .builder()
                                .currentChannelId(threadContainer.getIdLong())
                                .build();
                        validationResult.getValidationErrorModels().add(newError);
                    } else {
                        validationResult.setValidationResult(true);
                    }
                }
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
        } else {
            validationResult.setValidationResult(true);
        }
    }
}
