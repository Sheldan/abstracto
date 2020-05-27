package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
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

import java.util.Optional;
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
        Optional<AChannel> channel = channelManagementService.loadChannel(user.getChannelId());
        CompletableFuture<SetupStepResult> future = new CompletableFuture<>();
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(user.getGuildId(), user.getUserId());
        if(channel.isPresent()) {
            Runnable finalAction = super.getTimeoutRunnable(user.getGuildId(), user.getChannelId());
            Consumer<Void> confirmation = (Void none) -> {
                try {
                    self.executeDelayedSteps(parameter);
                    SetupStepResult result = SetupStepResult
                            .builder()
                            .result(SetupStepResultType.SUCCESS)
                            .build();
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            };

            Consumer<Void> denial = (Void none) -> {
                SetupStepResult result = SetupStepResult
                        .builder()
                        .result(SetupStepResultType.CANCELLED)
                        .build();
                future.complete(result);
            };
            interactiveService.createMessageWithConfirmation(messageToSend, aUserInAServer, channel.get(), parameter.getPreviousMessageId(), confirmation, denial, finalAction);
        } else {
            future.completeExceptionally(new ChannelNotFoundException(user.getGuildId(), user.getChannelId()));
        }
        return future;
    }

    @Transactional
    public void executeDelayedSteps(SetupSummaryStepParameter parameter) {
        delayedActionService.executeDelayedActions(parameter.getDelayedActionList());
    }
}
