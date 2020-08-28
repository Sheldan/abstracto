package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.interactive.*;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.models.template.commands.SetupCompletedNotificationModel;
import dev.sheldan.abstracto.core.models.template.commands.SetupInitialMessageModel;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

    @Autowired
    private ExceptionService exceptionService;

    @Override
    public CompletableFuture<Void> performSetup(FeatureConfig featureConfig, AServerChannelUserId user, Long initialMessageId) {
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
        if(textChannelInGuild.isPresent()) {
            TextChannel textChannel = textChannelInGuild.get();
            String text = templateService.renderTemplate("setup_initial_message", setupInitialMessageModel);
            channelService.sendTextToChannel(text, textChannel);
            return executeSetup(featureConfig, steps, user, new ArrayList<>());
        }
        throw new ChannelNotFoundException(user.getChannelId());
    }

    @Override
    public CompletableFuture<Void> executeSetup(FeatureConfig featureConfig, List<SetupExecution> steps, AServerChannelUserId user, List<DelayedActionConfig> delayedActionConfigs) {
        SetupExecution nextStep = steps.get(0);
        return executeStep(user, nextStep, delayedActionConfigs, featureConfig);
    }

    private CompletableFuture<Void> executeStep(AServerChannelUserId aUserInAServer, SetupExecution execution, List<DelayedActionConfig> delayedActionConfigs, FeatureConfig featureConfig) {
        return execution.getStep().execute(aUserInAServer, execution.getParameter()).thenAccept(aVoid -> {
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
        Optional<TextChannel> channelOptional = botService.getTextChannelFromServerOptional(aServerChannelUserId.getGuildId(), aServerChannelUserId.getChannelId());
        Member member = botService.getMemberInServer(aServerChannelUserId.getGuildId(), aServerChannelUserId.getUserId());
        channelOptional.ifPresent(textChannel -> exceptionService.reportExceptionToChannel(throwable, textChannel, member));
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
