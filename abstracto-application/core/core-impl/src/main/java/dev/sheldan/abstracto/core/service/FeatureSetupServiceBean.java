package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.exception.ChannelNotInGuildException;
import dev.sheldan.abstracto.core.interactive.*;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.models.template.commands.SetupCompletedNotificationModel;
import dev.sheldan.abstracto.core.models.template.commands.SetupInitialMessageModel;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
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
public class FeatureSetupServiceBean implements FeatureSetupService {

    public static final String FEATURE_SETUP_CANCELLATION_NOTIFICATION_TEMPLATE = "feature_setup_cancellation_notification";
    public static final String FEATURE_SETUP_COMPLETION_NOTIFICATION_TEMPLATE = "feature_setup_completion_notification";
    public static final String FEATURE_SETUP_INITIAL_MESSAGE_TEMPLATE_KEY = "feature_setup_initial_message";
    @Autowired
    private SystemConfigSetupStep systemConfigSetupStep;

    @Autowired
    private PostTargetSetupStep postTargetSetupStep;

    @Autowired
    private DelayedActionService delayedActionService;

    @Autowired
    private FeatureSetupServiceBean self;

    @Autowired
    private SetupSummaryStep setupSummaryStep;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private BotService botService;

    @Autowired
    private ExceptionService exceptionService;

    @Override
    public CompletableFuture<Void> performFeatureSetup(FeatureConfig featureConfig, AServerChannelUserId user, Long initialMessageId) {
        log.info("Performing setup of feature {} for user {} in channel {} in server {}.",
                featureConfig.getFeature().getKey(), user.getUserId(), user.getChannelId(), user.getGuildId());
        Optional<TextChannel> textChannelInGuild = channelService.getTextChannelFromServerOptional(user.getGuildId(), user.getChannelId());
        if(textChannelInGuild.isPresent()) {
            List<String> requiredSystemConfigKeys = featureConfig.getRequiredSystemConfigKeys();
            List<SetupExecution> steps = new ArrayList<>();
            requiredSystemConfigKeys.forEach(s -> {
                log.debug("Feature requires system config key {}.", s);
                SetupExecution execution = SetupExecution
                        .builder()
                        .step(systemConfigSetupStep)
                        .parameter(SystemConfigStepParameter.builder().configKey(s).build())
                        .build();
                steps.add(execution);
            });
            featureConfig.getRequiredPostTargets().forEach(postTargetEnum -> {
                log.debug("Feature requires post target {}.", postTargetEnum.getKey());
                SetupExecution execution = SetupExecution
                        .builder()
                        .step(postTargetSetupStep)
                        .parameter(PostTargetStepParameter.builder().postTargetKey(postTargetEnum.getKey()).build())
                        .build();
                steps.add(execution);
            });
            featureConfig.getCustomSetupSteps().forEach(setupStep -> {
                log.debug("Feature requires custom setup step {}.", setupStep.getClass().getName());
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
            TextChannel textChannel = textChannelInGuild.get();
            String text = templateService.renderTemplate(FEATURE_SETUP_INITIAL_MESSAGE_TEMPLATE_KEY, setupInitialMessageModel, user.getGuildId());
            channelService.sendTextToChannel(text, textChannel);
            ArrayList<DelayedActionConfig> delayedActionConfigs = new ArrayList<>();
            featureConfig
                    .getAutoSetupSteps()
                    .forEach(autoDelayedAction -> delayedActionConfigs.add(autoDelayedAction.getDelayedActionConfig(user)));
            return executeFeatureSetup(featureConfig, steps, user, delayedActionConfigs);
        }
        throw new ChannelNotInGuildException(user.getChannelId());
    }

    @Override
    public CompletableFuture<Void> executeFeatureSetup(FeatureConfig featureConfig, List<SetupExecution> steps, AServerChannelUserId user, List<DelayedActionConfig> delayedActionConfigs) {
        if(!steps.isEmpty()) {
            SetupExecution nextStep = steps.get(0);
            return executeStep(user, nextStep, delayedActionConfigs, featureConfig);
        } else {
            log.info("Feature had no setup steps. Executing post setups steps immediately. As there can be automatic steps.");
            self.executePostSetupSteps(delayedActionConfigs, user, null, featureConfig);
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<Void> executeStep(AServerChannelUserId aUserInAServer, SetupExecution execution, List<DelayedActionConfig> delayedActionConfigs, FeatureConfig featureConfig) {
        log.debug("Executing step {} in server {} in channel {} for user {}.", execution.getStep().getClass(), aUserInAServer.getGuildId(), aUserInAServer.getChannelId(), aUserInAServer.getUserId());
        return execution.getStep().execute(aUserInAServer, execution.getParameter()).thenAccept(setpResult -> {
            if(setpResult.getResult().equals(SetupStepResultType.SUCCESS)) {
                log.info("Step {} in server {} has been executed successfully. Proceeding.", execution.getStep().getClass(), aUserInAServer.getGuildId());
                delayedActionConfigs.addAll(setpResult.getDelayedActionConfigList());
                if(execution.getNextStep() != null) {
                    log.debug("Executing next step {}.", execution.getNextStep().getStep().getClass());
                    executeStep(aUserInAServer, execution.getNextStep(), delayedActionConfigs, featureConfig);
                } else {
                    log.debug("Step was the last step. Executing post setup steps.");
                    self.executePostSetupSteps(delayedActionConfigs, aUserInAServer, execution.getParameter().getPreviousMessageId(), featureConfig);
                }
            } else {
                log.info("Result of step {} has been {}. Notifying user.", execution.getStep().getClass(), SetupStepResultType.CANCELLED);
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
        Optional<TextChannel> channelOptional = channelService.getTextChannelFromServerOptional(aServerChannelUserId.getGuildId(), aServerChannelUserId.getChannelId());
        memberService.getMemberInServerAsync(aServerChannelUserId.getGuildId(), aServerChannelUserId.getUserId()).thenAccept(member ->
            channelOptional.ifPresent(textChannel -> exceptionService.reportExceptionToChannel(throwable, textChannel, member))
        ).exceptionally(innserThrowable -> {
            log.error("Failed to report exception message for exception {} for user {} in channel {} in server {}.", throwable, aServerChannelUserId.getUserId(), aServerChannelUserId.getChannelId(), aServerChannelUserId.getGuildId(), innserThrowable);
            return null;
        });
    }

    @Transactional
    public void executePostSetupSteps(List<DelayedActionConfig> delayedActionConfigs, AServerChannelUserId user, Long initialMessage, FeatureConfig featureConfig) {
        SetupSummaryStepParameter parameter = SetupSummaryStepParameter
                .builder()
                .delayedActionList(delayedActionConfigs)
                .previousMessageId(initialMessage)
                .build();
        setupSummaryStep.execute(user, parameter).thenAccept(setupStepResult -> self.notifyAboutCompletion(user, featureConfig, setupStepResult));
    }

    @Transactional
    public void notifyAboutCompletion(AServerChannelUserId aServerChannelUserId, FeatureConfig featureConfig, SetupStepResult result) {
        log.debug("Notifying user {} in channel {} in server {} about completion of setup for feature {}.",
                aServerChannelUserId.getUserId(), aServerChannelUserId.getChannelId(), aServerChannelUserId.getGuildId(), featureConfig.getFeature().getKey());
        String templateKey;
        if(result.getResult().equals(SetupStepResultType.CANCELLED)) {
            templateKey = FEATURE_SETUP_CANCELLATION_NOTIFICATION_TEMPLATE;
        } else {
            templateKey = FEATURE_SETUP_COMPLETION_NOTIFICATION_TEMPLATE;
        }
        notifyUserWithTemplate(aServerChannelUserId, featureConfig, templateKey);
    }

    private void notifyUserWithTemplate(AServerChannelUserId aServerChannelUserId, FeatureConfig featureConfig, String templateName) {
        SetupCompletedNotificationModel model = SetupCompletedNotificationModel
                .builder()
                .featureConfig(featureConfig)
                .build();
        String text = templateService.renderTemplate(templateName, model, aServerChannelUserId.getGuildId());
        Optional<TextChannel> textChannel = channelService.getTextChannelFromServerOptional(aServerChannelUserId.getGuildId(), aServerChannelUserId.getChannelId());
        textChannel.ifPresent(channel -> channelService.sendTextToChannel(text, channel));
    }

    @Transactional
    public void notifyAboutCancellation(AServerChannelUserId aServerChannelUserId, FeatureConfig featureConfig) {
        log.debug("Notifying user {} in channel {} in server {} about cancellation of setup for feature {}.",
                aServerChannelUserId.getUserId(), aServerChannelUserId.getChannelId(), aServerChannelUserId.getGuildId(), featureConfig.getFeature().getKey());
        notifyUserWithTemplate(aServerChannelUserId, featureConfig, FEATURE_SETUP_CANCELLATION_NOTIFICATION_TEMPLATE);
    }
}
