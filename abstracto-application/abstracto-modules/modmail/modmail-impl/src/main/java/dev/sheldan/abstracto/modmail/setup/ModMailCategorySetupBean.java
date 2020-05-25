package dev.sheldan.abstracto.modmail.setup;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.interactive.*;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AConfig;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.modmail.models.template.SetupModMailCategoryMessageModel;
import dev.sheldan.abstracto.modmail.service.ModMailThreadServiceBean;
import dev.sheldan.abstracto.modmail.validator.ModMailFeatureValidator;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
    private BotService botService;

    @Override
    public CompletableFuture<SetupStepResult> execute(AServerChannelUserId user, SetupStepParameter parameter) {
        String messageTemplateKey = "setup_modmail_category_message";
        SetupModMailCategoryMessageModel model = SetupModMailCategoryMessageModel
                .builder()
                .build();
        if(configManagementService.configExists(user.getGuildId(), ModMailThreadServiceBean.MODMAIL_CATEGORY)) {
            Guild guild = botService.getGuildByIdNullable(user.getGuildId());
            Long categoryId = configService.getLongValue(ModMailThreadServiceBean.MODMAIL_CATEGORY, user.getGuildId());
            Category category = guild.getCategoryById(categoryId);
            model.setCategory(category);
        }
        String messageText = templateService.renderTemplate(messageTemplateKey, model);
        Optional<AChannel> channel = channelManagementService.loadChannel(user.getChannelId());
        CompletableFuture<SetupStepResult> future = new CompletableFuture<>();
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(user.getGuildId(), user.getUserId());

        if(channel.isPresent()) {
            Runnable finalAction = getTimeoutRunnable(user.getGuildId(), user.getChannelId());
            Consumer<MessageReceivedEvent> configAction = (MessageReceivedEvent event) -> {
                try {

                    SetupStepResult result;
                    Message message = event.getMessage();
                    if(checkForExit(message)) {
                        result = SetupStepResult.fromCancelled();
                    } else {
                        String messageContent = event.getMessage().getContentRaw();
                        Long categoryId = Long.parseLong(messageContent);
                        Guild guild = botService.getGuildByIdNullable(user.getGuildId());
                        FeatureValidationResult featureValidationResult = FeatureValidationResult.builder().validationResult(true).build();
                        modMailFeatureValidator.validateModMailCategory(featureValidationResult, guild, categoryId);
                        if(featureValidationResult.getValidationResult()) {
                            AConfig fakeValue = configService.getFakeConfigForValue(ModMailThreadServiceBean.MODMAIL_CATEGORY, user.getGuildId(), messageContent);
                            ModMailCategoryDelayedActionConfig build = ModMailCategoryDelayedActionConfig
                                    .builder()
                                    .serverId(user.getGuildId())
                                    .category(guild.getCategoryById(categoryId))
                                    .value(fakeValue)
                                    .build();
                            List<DelayedActionConfig> delayedSteps = Arrays.asList(build);
                            result = SetupStepResult
                                    .builder()
                                    .result(SetupStepResultType.SUCCESS)
                                    .delayedActionConfigList(delayedSteps)
                                    .build();
                        } else {
                            throw new AbstractoRunTimeException("Category id does not conform.");
                        }

                    }

                    future.complete(result);
                } catch (Exception e) {
                    log.error("Failed to handle post target step.", e);
                    future.completeExceptionally(e);
                }
            };
            interactiveService.createMessageWithResponse(messageText, aUserInAServer, channel.get(), parameter.getPreviousMessageId(), configAction, finalAction);
        } else {
            future.completeExceptionally(new ChannelNotFoundException(user.getGuildId(), user.getChannelId()));
        }
        return future;
    }

    protected Runnable getTimeoutRunnable(Long serverId, Long channelId) {
        return () -> {
            interactiveUtils.sendTimeoutMessage(serverId, channelId);
        };
    }

    protected boolean checkForExit(Message message) {
        return message.getContentRaw().trim().equalsIgnoreCase("exit");
    }

}
