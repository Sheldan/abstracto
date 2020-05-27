package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.interactive.*;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.models.template.commands.SetupCompletedNotificationModel;
import dev.sheldan.abstracto.core.models.template.commands.SetupInitialMessageModel;
import dev.sheldan.abstracto.templating.Templatable;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class SetupServiceBean implements SetupService {

    @Autowired
    private SystemConfigSetupStep systemConfigSetupStep;

    @Autowired
    private PostTargetSetupStep postTargetSetupStep;

    @Autowired
    private DelayedActionService delayedActionService;

    @Autowired
    private SetupServiceBean self;

    @Autowired
    private SetupSummaryStep setupSummaryStep;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private BotService botService;

    @Override
    public void performSetup(FeatureConfig featureConfig, AServerChannelUserId user, Long initialMessageId) {
        List<String> requiredSystemConfigKeys = featureConfig.getRequiredSystemConfigKeys();
        List<SetupExecution> steps = new ArrayList<>();
        requiredSystemConfigKeys.forEach(s -> {
            SetupExecution execution = SetupExecution
                    .builder()
                    .step(systemConfigSetupStep)
                    .parameter(SystemConfigStepParameter.builder().configKey(s).build())
                    .build();
            steps.add(execution);
        });
        featureConfig.getRequiredPostTargets().forEach(postTargetEnum -> {
            SetupExecution execution = SetupExecution
                    .builder()
                    .step(postTargetSetupStep)
                    .parameter(PostTargetStepParameter.builder().postTargetKey(postTargetEnum.getKey()).build())
                    .build();
            steps.add(execution);
        });
        featureConfig.getCustomSetupSteps().forEach(setupStep -> {
            SetupExecution execution = SetupExecution
                    .builder()
                    .step(setupStep)
                    .parameter(EmptySetupParameter.builder().build())
                    .build();
            steps.add(execution);
        });
        for (int i = 0; i < steps.size(); i++) {
            SetupExecution setupExecution = steps.get(i);
            setupExecution.getParameter().setPreviousMessageId(initialMessageId);
            if(i < steps.size() - 1) {
                setupExecution.setNextStep(steps.get(i + 1));
            }
        }

        SetupInitialMessageModel setupInitialMessageModel = SetupInitialMessageModel
                .builder()
                .featureConfig(featureConfig)
                .build();
        Optional<TextChannel> textChannelInGuild = channelService.getTextChannelInGuild(user.getGuildId(), user.getChannelId());
        textChannelInGuild.ifPresent(textChannel -> {
            String text = templateService.renderTemplate("setup_initial_message", setupInitialMessageModel);
            channelService.sendTextToChannel(text, textChannel);
            executeSetup(featureConfig, steps, user, new ArrayList<>());
        });

    }

    @Override
    public void executeSetup(FeatureConfig featureConfig, List<SetupExecution> steps, AServerChannelUserId user, List<DelayedActionConfig> delayedActionConfigs) {
        steps.stream().findFirst().ifPresent(execution -> executeStep(user, execution, delayedActionConfigs, featureConfig));
    }

    private void executeStep(AServerChannelUserId aUserInAServer, SetupExecution execution, List<DelayedActionConfig> delayedActionConfigs, FeatureConfig featureConfig) {
        execution.getStep().execute(aUserInAServer, execution.getParameter()).thenAccept(aVoid -> {
            if(aVoid.getResult().equals(SetupStepResultType.SUCCESS)) {
                delayedActionConfigs.addAll(aVoid.getDelayedActionConfigList());
                if(execution.getNextStep() != null) {
                    executeStep(aUserInAServer, execution.getNextStep(), delayedActionConfigs, featureConfig);
                } else {
                    self.executePostSetupSteps(delayedActionConfigs, aUserInAServer, execution.getParameter().getPreviousMessageId(), featureConfig);
                }
            } else {
                self.notifyAboutCancellation(aUserInAServer, featureConfig);
            }

        }).exceptionally(throwable -> {
            showExceptionMessage(throwable.getCause(), aUserInAServer);
            executeStep(aUserInAServer, execution, delayedActionConfigs, featureConfig);
            return null;
        });
    }

    @Transactional
    public void showExceptionMessage(Throwable throwable, AServerChannelUserId aServerChannelUserId) {
        if(throwable instanceof Templatable) {
            Templatable exception = (Templatable) throwable;
            String text = templateService.renderTemplate(exception.getTemplateName(), exception.getTemplateModel());
            Optional<TextChannel> channelOptional = botService.getTextChannelFromServer(aServerChannelUserId.getGuildId(), aServerChannelUserId.getChannelId());
            channelOptional.ifPresent(channel -> channelService.sendTextToChannel(text, channel));
        }
    }

    @Transactional
    public void executePostSetupSteps(List<DelayedActionConfig> delayedActionConfigs, AServerChannelUserId user, Long initialMessage, FeatureConfig featureConfig) {
        SetupSummaryStepParameter parameter = SetupSummaryStepParameter
                .builder()
                .delayedActionList(delayedActionConfigs)
                .previousMessageId(initialMessage)
                .build();
        setupSummaryStep.execute(user, parameter).thenAccept(ignored -> self.notifyAboutCompletion(user, featureConfig));
    }

    @Transactional
    public void notifyAboutCompletion(AServerChannelUserId aServerChannelUserId, FeatureConfig featureConfig) {
        notifyUserWithTemplate(aServerChannelUserId, featureConfig, "setup_completion_notification");
    }

    private void notifyUserWithTemplate(AServerChannelUserId aServerChannelUserId, FeatureConfig featureConfig, String templateName) {
        SetupCompletedNotificationModel model = SetupCompletedNotificationModel
                .builder()
                .featureConfig(featureConfig)
                .build();
        String text = templateService.renderTemplate(templateName, model);
        Optional<TextChannel> textChannel = channelService.getTextChannelInGuild(aServerChannelUserId.getGuildId(), aServerChannelUserId.getChannelId());
        textChannel.ifPresent(channel -> channelService.sendTextToChannel(text, channel));
    }

    @Transactional
    public void notifyAboutCancellation(AServerChannelUserId aServerChannelUserId, FeatureConfig featureConfig) {
        notifyUserWithTemplate(aServerChannelUserId, featureConfig, "setup_cancellation_notification");
    }
}
