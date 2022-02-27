package dev.sheldan.abstracto.modmail.setup;

import dev.sheldan.abstracto.core.interactive.*;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.modmail.model.template.SetupModMailCategoryMessageModel;
import dev.sheldan.abstracto.modmail.service.ModMailThreadServiceBean;
import dev.sheldan.abstracto.modmail.validator.ModMailFeatureValidator;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Component
@Slf4j
public class ModMailCategorySetupBean implements ModMailCategorySetup {

    @Autowired
    private InteractiveUtils interactiveUtils;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private InteractiveService interactiveService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ConfigManagementService configManagementService;

    @Autowired
    private ModMailFeatureValidator modMailFeatureValidator;

    @Autowired
    private GuildService guildService;

    /**
     * This setup method loads the existing mod mail category (if anything) and populates the model used to render the prompt.
     * This method will then render the prompt and wait for the users input, if the provided input was a valid
     * category ID in the current server, this method returns the proper result. If anything else is put in (except the input
     * triggering a cancellation) this this method will jump back to this step and prompt the user again.
     * @param user The {@link AServerChannelUserId} context required in order to execute the step. This is needed
     *             to check if the returned message is from the same user and to see for which server
     *             we need to change the mod mail category for
     * @param parameter This is a parameter which contains the previous message triggering the setup step.
     *                  This is necessary, because sometimes the {@link Message} executing the setup was also triggering
     *                  the first {@link SetupStep}, so, if we are aware of it, we can ignore it
     * @return A {@link CompletableFuture} containing the {@link SetupStepResult}. This might be cancelled or successful.
     */
    @Override
    public CompletableFuture<SetupStepResult> execute(AServerChannelUserId user, SetupStepParameter parameter) {
        String messageTemplateKey = "feature_setup_modmail_category_message";
        SetupModMailCategoryMessageModel model = SetupModMailCategoryMessageModel
                .builder()
                .build();
        if(configManagementService.configExists(user.getGuildId(), ModMailThreadServiceBean.MODMAIL_CATEGORY)) {
            Guild guild = guildService.getGuildById(user.getGuildId());
            Long categoryId = configService.getLongValue(ModMailThreadServiceBean.MODMAIL_CATEGORY, user.getGuildId());
            log.debug("Previous modmail category exists for server {}. Loading value {}.", guild.getId(), categoryId);
            Category category = guild.getCategoryById(categoryId);
            if(category != null) {
                model.setCategoryId(category.getIdLong());
            }
            model.setServerId(user.getGuildId());
        }
        log.info("Executing mod mail category setup for server {}.", user.getGuildId());
        String messageText = templateService.renderTemplate(messageTemplateKey, model);
        AChannel channel = channelManagementService.loadChannel(user.getChannelId());
        CompletableFuture<SetupStepResult> future = new CompletableFuture<>();
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(user.getGuildId(), user.getUserId());

        Consumer<MessageReceivedModel> finalAction = getTimeoutConsumer(user.getGuildId(), user.getChannelId());
        Consumer<MessageReceivedModel> configAction = (MessageReceivedModel event) -> {
            try {

                SetupStepResult result;
                Message message = event.getMessage();
                // this checks whether or not the user wanted to cancel the setup
                if(checkForExit(message)) {
                    log.info("User {} wants to exit modmail category setup for server {}.", user.getUserId(), user.getGuildId());
                    result = SetupStepResult.fromCancelled();
                } else {
                    String messageContent = event.getMessage().getContentRaw();
                    // directly parse the long from the message, for *now*, only the category ID is supported
                    Long categoryId = Long.parseLong(messageContent);
                    Guild guild = guildService.getGuildById(user.getGuildId());
                    FeatureValidationResult featureValidationResult = FeatureValidationResult.builder().validationResult(true).build();
                    // directly validate whether or not the given category ID is a valid value
                    modMailFeatureValidator.validateModMailCategory(featureValidationResult, guild, categoryId);
                    if(Boolean.TRUE.equals(featureValidationResult.getValidationResult())) {
                        log.debug("Given category {} maps to a valid category in server {}.", categoryId, guild.getId());
                        ModMailCategoryDelayedActionConfig build = ModMailCategoryDelayedActionConfig
                                .builder()
                                .serverId(user.getGuildId())
                                .categoryId(categoryId)
                                .build();
                        DelayedActionConfigContainer container = DelayedActionConfigContainer
                                .builder()
                                .type(build.getClass())
                                .object(build)
                                .build();
                        List<DelayedActionConfigContainer> delayedSteps = Arrays.asList(container);
                        result = SetupStepResult
                                .builder()
                                .result(SetupStepResultType.SUCCESS)
                                .delayedActionConfigList(delayedSteps)
                                .build();
                    } else {
                        // exceptions this exception is used to effectively fail the setup step
                        throw new InvalidCategoryException();
                    }

                }

                future.complete(result);
            } catch (Exception e) {
                log.error("Failed to handle mod mail category step.", e);
                future.completeExceptionally(new SetupStepException(e));
            }
        };
        interactiveService.createMessageWithResponse(messageText, aUserInAServer, channel, configAction, finalAction);
        return future;
    }

    protected Consumer<MessageReceivedModel> getTimeoutConsumer(Long serverId, Long channelId) {
        return (MessageReceivedModel) -> interactiveUtils.sendTimeoutMessage(serverId, channelId);
    }

    protected boolean checkForExit(Message message) {
        return message.getContentRaw().trim().equalsIgnoreCase("exit");
    }

}
