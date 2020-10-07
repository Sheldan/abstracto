package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.commands.SetupSummaryModel;
import dev.sheldan.abstracto.core.service.DelayedActionService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Component
@Slf4j
public class SetupSummaryStep extends AbstractConfigSetupStep {

    @Autowired
    private InteractiveService interactiveService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private DelayedActionService delayedActionService;

    @Autowired
    private SetupSummaryStep self;

    @Override
    public CompletableFuture<SetupStepResult> execute(AServerChannelUserId user, SetupStepParameter generalParameter) {
        SetupSummaryStepParameter parameter = (SetupSummaryStepParameter) generalParameter;
        SetupSummaryModel model = SetupSummaryModel
                .builder()
                .actionConfigs(parameter.getDelayedActionList())
                .build();
        String messageToSend = templateService.renderTemplate("setup_confirmation", model);
        AChannel channel = channelManagementService.loadChannel(user.getChannelId());
        CompletableFuture<SetupStepResult> future = new CompletableFuture<>();
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(user.getGuildId(), user.getUserId());
        Runnable finalAction = super.getTimeoutRunnable(user.getGuildId(), user.getChannelId());
        log.info("Executing setup summary question step in server {} in channel {} from user {}.", user.getGuildId(), user.getChannelId(), user.getUserId());
        Consumer<Void> confirmation = (Void none) -> {
            try {
                log.info("Setup summary was confirmed. Executing {} steps.", parameter.getDelayedActionList().size());
                self.executeDelayedSteps(parameter);
                SetupStepResult result = SetupStepResult
                        .builder()
                        .result(SetupStepResultType.SUCCESS)
                        .build();
                future.complete(result);
            } catch (Exception e) {
                log.error("Failed to execute {} delayed actions.", parameter.getDelayedActionList().size(), e);
                future.completeExceptionally(e);
            }
        };

        Consumer<Void> denial = (Void none) -> {
            log.info("Setup summary was rejected. Cancelling execution of {} steps.", parameter.getDelayedActionList().size());
            SetupStepResult result = SetupStepResult
                    .builder()
                    .result(SetupStepResultType.CANCELLED)
                    .build();
            future.complete(result);
        };
        interactiveService.createMessageWithConfirmation(messageToSend, aUserInAServer, channel, parameter.getPreviousMessageId(), confirmation, denial, finalAction);
        return future;
    }

    @Transactional
    public void executeDelayedSteps(SetupSummaryStepParameter parameter) {
        delayedActionService.executeDelayedActions(parameter.getDelayedActionList());
    }
}
