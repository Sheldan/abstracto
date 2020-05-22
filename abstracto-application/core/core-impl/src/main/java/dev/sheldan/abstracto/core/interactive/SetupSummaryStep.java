package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.commands.SetupSummaryModel;
import dev.sheldan.abstracto.core.service.DelayedActionService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
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
    public CompletableFuture<List<DelayedActionConfig>> execute(AServerChannelUserId user, SetupStepParameter generalParameter) {
        SetupSummaryStepParameter parameter = (SetupSummaryStepParameter) generalParameter;
        SetupSummaryModel model = SetupSummaryModel
                .builder()
                .actionConfigs(parameter.getDelayedActionList())
                .build();
        String messageToSend = templateService.renderTemplate("setup_confirmation", model);
        Optional<AChannel> channel = channelManagementService.loadChannel(user.getChannelId());
        CompletableFuture<List<DelayedActionConfig>> future = new CompletableFuture<>();
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(user.getGuildId(), user.getUserId());
        if(channel.isPresent()) {
            Runnable finalAction = super.getTimeoutRunnable(user.getGuildId(), user.getChannelId());
            Consumer<Void> confirmation = (Void none) -> {
                try {
                    self.executeDelayedSteps(parameter);
                    future.complete(null);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            };

            Consumer<Void> denial = (Void none) -> {
               log.info("Stopped wizard.");
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
