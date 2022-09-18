package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.interactive.*;
import dev.sheldan.abstracto.core.interactive.setup.step.PostTargetSetupStep;
import dev.sheldan.abstracto.core.interactive.setup.step.SetupSummaryStep;
import dev.sheldan.abstracto.core.interactive.setup.step.SystemConfigSetupStep;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.models.template.commands.SetupCompletedNotificationModel;
import dev.sheldan.abstracto.core.models.template.commands.SetupInitialMessageModel;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private ExceptionService exceptionService;

    @Override
    public CompletableFuture<Void> performFeatureSetup(FeatureConfig featureConfig, AServerChannelUserId user, Long initialMessageId) {
        log.info("Performing setup of feature {} for user {} in channel {} in server {}.",
                featureConfig.getFeature().getKey(), user.getUserId(), user.getChannelId(), user.getGuildId());
        GuildMessageChannel messageChannelInGuild = channelService.getMessageChannelFromServer(user.getGuildId(), user.getChannelId());
        Set<String> requiredSystemConfigKeys = new HashSet<>();
        Set<PostTargetEnum> requiredPostTargets = new HashSet<>();
        Set<SetupStep> customSetupSteps = new HashSet<>();

        collectRequiredFeatureSteps(featureConfig, requiredSystemConfigKeys, requiredPostTargets, customSetupSteps, new HashSet<>());

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
        requiredPostTargets.forEach(postTargetEnum -> {
            log.debug("Feature requires post target {}.", postTargetEnum.getKey());
            SetupExecution execution = SetupExecution
                    .builder()
                    .step(postTargetSetupStep)
                    .parameter(PostTargetStepParameter.builder().postTargetKey(postTargetEnum.getKey()).build())
                    .build();
            steps.add(execution);
        });
        customSetupSteps.forEach(setupStep -> {
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
            if (i < steps.size() - 1) {
                setupExecution.setNextStep(steps.get(i + 1));
            }
        }

        SetupInitialMessageModel setupInitialMessageModel = SetupInitialMessageModel
                .builder()
                .featureConfig(featureConfig)
                .build();
        String text = templateService.renderTemplate(FEATURE_SETUP_INITIAL_MESSAGE_TEMPLATE_KEY, setupInitialMessageModel, user.getGuildId());
        channelService.sendTextToChannel(text, messageChannelInGuild);
        ArrayList<DelayedActionConfigContainer> delayedActionConfigs = new ArrayList<>();
        featureConfig
                .getAutoSetupSteps()
                .forEach(autoStep -> {
                    DelayedActionConfig autoDelayedAction = autoStep.getDelayedActionConfig(user);
                    DelayedActionConfigContainer container = DelayedActionConfigContainer
                            .builder()
                            .object(autoDelayedAction)
                            .type(autoDelayedAction.getClass())
                            .build();
                    delayedActionConfigs.add(container);
                });
        return executeFeatureSetup(featureConfig, steps, user, delayedActionConfigs);
    }

    @Override
    public CompletableFuture<Void> executeFeatureSetup(FeatureConfig featureConfig, List<SetupExecution> steps, AServerChannelUserId user, List<DelayedActionConfigContainer> delayedActionConfigs) {
        if (!steps.isEmpty()) {
            SetupExecution nextStep = steps.get(0);
            return executeStep(user, nextStep, delayedActionConfigs, featureConfig);
        } else {
            log.info("Feature had no setup steps. Executing post setups steps immediately. As there can be automatic steps.");
            self.executePostSetupSteps(delayedActionConfigs, user, null, featureConfig);
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<Void> executeStep(AServerChannelUserId aUserInAServer, SetupExecution execution, List<DelayedActionConfigContainer> delayedActionConfigs, FeatureConfig featureConfig) {
        log.debug("Executing step {} in server {} in channel {} for user {}.", execution.getStep().getClass(), aUserInAServer.getGuildId(), aUserInAServer.getChannelId(), aUserInAServer.getUserId());
        return execution.getStep().execute(aUserInAServer, execution.getParameter()).thenAccept(setpResult -> {
            if (setpResult.getResult().equals(SetupStepResultType.SUCCESS)) {
                log.info("Step {} in server {} has been executed successfully. Proceeding.", execution.getStep().getClass(), aUserInAServer.getGuildId());
                delayedActionConfigs.addAll(setpResult.getDelayedActionConfigList());
                if (execution.getNextStep() != null) {
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
        GuildMessageChannel messageChannelInGuild = channelService.getMessageChannelFromServer(aServerChannelUserId.getGuildId(), aServerChannelUserId.getChannelId());
        memberService.getMemberInServerAsync(aServerChannelUserId.getGuildId(), aServerChannelUserId.getUserId()).thenAccept(member ->
               exceptionService.reportExceptionToChannel(throwable, messageChannelInGuild, member)
        ).exceptionally(innserThrowable -> {
            log.error("Failed to report exception message for exception {} for user {} in channel {} in server {}.", throwable, aServerChannelUserId.getUserId(), aServerChannelUserId.getChannelId(), aServerChannelUserId.getGuildId(), innserThrowable);
            return null;
        });
    }

    @Transactional
    public void executePostSetupSteps(List<DelayedActionConfigContainer> delayedActionConfigs, AServerChannelUserId user, Long initialMessage, FeatureConfig featureConfig) {
        SetupSummaryStepParameter parameter = SetupSummaryStepParameter
                .builder()
                .delayedActionList(delayedActionConfigs)
                .featureConfig(featureConfig)
                .previousMessageId(initialMessage)
                .build();
        setupSummaryStep.execute(user, parameter)
        .exceptionally(throwable -> {
            showExceptionMessage(throwable.getCause(), user);
            return null;
        });
    }

    @Transactional
    public void notifyAboutCompletion(AServerChannelUserId aServerChannelUserId, String featureKey, SetupStepResult result) {
        log.debug("Notifying user {} in channel {} in server {} about completion of setup for feature {}.",
                aServerChannelUserId.getUserId(), aServerChannelUserId.getChannelId(), aServerChannelUserId.getGuildId(), featureKey);
        String templateKey;
        if (result.getResult().equals(SetupStepResultType.CANCELLED)) {
            templateKey = FEATURE_SETUP_CANCELLATION_NOTIFICATION_TEMPLATE;
        } else {
            templateKey = FEATURE_SETUP_COMPLETION_NOTIFICATION_TEMPLATE;
        }
        notifyUserWithTemplate(aServerChannelUserId, featureKey, templateKey);
    }

    private void notifyUserWithTemplate(AServerChannelUserId aServerChannelUserId, String featureKey, String templateName) {
        SetupCompletedNotificationModel model = SetupCompletedNotificationModel
                .builder()
                .featureKey(featureKey)
                .build();
        String text = templateService.renderTemplate(templateName, model, aServerChannelUserId.getGuildId());
        GuildMessageChannel messageChannelInGuild = channelService.getMessageChannelFromServer(aServerChannelUserId.getGuildId(), aServerChannelUserId.getChannelId());
        channelService.sendTextToChannel(text, messageChannelInGuild);
    }

    @Transactional
    public void notifyAboutCancellation(AServerChannelUserId aServerChannelUserId, FeatureConfig featureConfig) {
        log.debug("Notifying user {} in channel {} in server {} about cancellation of setup for feature {}.",
                aServerChannelUserId.getUserId(), aServerChannelUserId.getChannelId(), aServerChannelUserId.getGuildId(), featureConfig.getFeature().getKey());
        notifyUserWithTemplate(aServerChannelUserId, featureConfig.getFeature().getKey(), FEATURE_SETUP_CANCELLATION_NOTIFICATION_TEMPLATE);
    }

    private void collectRequiredFeatureSteps(FeatureConfig featureConfig, Set<String> requiredSystemConfigKeys,
                                             Set<PostTargetEnum> requiredPostTargets, Set<SetupStep> customSetupSteps,
                                             Set<String> coveredFeatures) {
        if (coveredFeatures.contains(featureConfig.getFeature().getKey())) {
            return;
        }
        coveredFeatures.add(featureConfig.getFeature().getKey());
        requiredSystemConfigKeys.addAll(featureConfig.getRequiredSystemConfigKeys());
        requiredPostTargets.addAll(featureConfig.getRequiredPostTargets());
        customSetupSteps.addAll(featureConfig.getCustomSetupSteps());
        featureConfig.getRequiredFeatures()
                .forEach(requiredFeature -> collectRequiredFeatureSteps(requiredFeature, requiredSystemConfigKeys, requiredPostTargets, customSetupSteps, coveredFeatures));
    }

}
