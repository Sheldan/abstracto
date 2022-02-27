package dev.sheldan.abstracto.core.interactive.setup.step;

import dev.sheldan.abstracto.core.interactive.*;
import dev.sheldan.abstracto.core.interactive.setup.payload.SetupConfirmationPayload;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.commands.SetupSummaryModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.ComponentPayloadService;
import dev.sheldan.abstracto.core.service.ComponentService;
import dev.sheldan.abstracto.core.service.DelayedActionService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class SetupSummaryStep extends AbstractConfigSetupStep {

    public static final String FEATURE_SETUP_CONFIRMATION_TEMPLATE_KEY = "feature_setup_confirmation";
    public static final String SETUP_SUMMARY_ORIGIN = "setupSummary";
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

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ComponentPayloadService componentPayloadService;

    @Override
    public CompletableFuture<SetupStepResult> execute(AServerChannelUserId user, SetupStepParameter generalParameter) {
        SetupSummaryStepParameter parameter = (SetupSummaryStepParameter) generalParameter;
        SetupSummaryModel model = SetupSummaryModel
                .builder()
                .actionConfigs(parameter.getDelayedActionList())
                .build();
        String confirmId = componentService.generateComponentId();
        String abortId = componentService.generateComponentId();
        model.setCancelButtonId(abortId);
        model.setConfirmButtonId(confirmId);
        MessageToSend message = templateService.renderEmbedTemplate(FEATURE_SETUP_CONFIRMATION_TEMPLATE_KEY, model, user.getGuildId());
        AChannel channel = channelManagementService.loadChannel(user.getChannelId());
        List<CompletableFuture<Message>> confirmationMessageFutures = channelService.sendMessageEmbedToSendToAChannel(message, channel);
        return FutureUtils.toSingleFutureGeneric(confirmationMessageFutures)
                .thenAccept(unused -> self.persistConfirmationCallbacks(model, user, parameter))
                .thenApply(unused -> SetupStepResult.builder().result(SetupStepResultType.SUCCESS).build());
    }

    @Transactional
    public void persistConfirmationCallbacks(SetupSummaryModel model, AServerChannelUserId origin, SetupSummaryStepParameter parameter) {
        AServer server = serverManagementService.loadServer(origin.getGuildId());

        SetupConfirmationPayload confirmPayload = SetupConfirmationPayload
                .builder()
                .otherButtonComponentId(model.getConfirmButtonId())
                .origin(origin)
                .actions(model.getActionConfigs())
                .featureKey(parameter.getFeatureConfig().getFeature().getKey())
                .action(SetupConfirmationPayload.SetupConfirmationAction.CONFIRM)
                .build();

        componentPayloadService.createButtonPayload(model.getConfirmButtonId(), confirmPayload, SETUP_SUMMARY_ORIGIN, server);
        SetupConfirmationPayload cancelPayload = SetupConfirmationPayload
                .builder()
                .otherButtonComponentId(model.getCancelButtonId())
                .origin(origin)
                .actions(model.getActionConfigs())
                .featureKey(parameter.getFeatureConfig().getFeature().getKey())
                .action(SetupConfirmationPayload.SetupConfirmationAction.ABORT)
                .build();

        componentPayloadService.createButtonPayload(model.getCancelButtonId(), cancelPayload, SETUP_SUMMARY_ORIGIN, server);
    }

}
