package dev.sheldan.abstracto.moderation.listener.interaction;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.MessageContextConfig;
import dev.sheldan.abstracto.core.interaction.modal.ModalConfigPayload;
import dev.sheldan.abstracto.core.interaction.modal.ModalService;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.interaction.context.message.listener.MessageContextCommandListener;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.interaction.MessageContextInteractionModel;
import dev.sheldan.abstracto.core.interaction.context.ContextCommandService;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.interaction.MessageReportModalPayload;
import dev.sheldan.abstracto.moderation.model.template.listener.ReportInputModalModel;
import dev.sheldan.abstracto.moderation.service.ReactionReportService;
import dev.sheldan.abstracto.moderation.service.ReactionReportServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class ReportWithContextContextCommandListener implements MessageContextCommandListener {

    @Autowired
    private ContextCommandService contextCommandService;

    @Autowired
    private ModalService modalService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private ReportWithContextContextCommandListener self;

    @Autowired
    private ReactionReportService reactionReportService;

    @Autowired
    private InteractionService interactionService;

    private static final String REACTION_REPORT_MODAL_TEMPLATE = "reactionReport_input";

    @Override
    public DefaultListenerResult execute(MessageContextInteractionModel model) {
        Message targetMessage = model.getEvent().getTarget();
        if(targetMessage.getAuthor().getIdLong()  == model.getEvent().getUser().getIdLong()) {
            interactionService.replyEmbed(ReactionReportServiceBean.REACTION_REPORT_OWN_MESSAGE_RESPONSE_TEMPLATE, new Object(), model.getEvent());
            return DefaultListenerResult.IGNORED;
        }
        ServerUser userReporting = ServerUser
                .builder()
                .serverId(model.getServerId())
                .userId(model.getEvent().getUser().getIdLong())
                .isBot(model.getEvent().getUser().isBot())
                .build();
        if(!reactionReportService.allowedToReport(userReporting)) {
            log.info("User {} was reported on message {} in server {} within the cooldown. Ignoring.",
                    targetMessage.getAuthor().getIdLong(), targetMessage.getIdLong(), targetMessage.getGuild().getIdLong());
            interactionService.replyEmbed(ReactionReportServiceBean.REACTION_REPORT_COOLDOWN_RESPONSE_TEMPLATE, new Object(), model.getEvent());
            return DefaultListenerResult.IGNORED;
        }

        String modalId = componentService.generateComponentId();
        String textInputId = componentService.generateComponentId();

        ReportInputModalModel modalModel = ReportInputModalModel
                .builder()
                .modalId(modalId)
                .inputComponentId(textInputId)
                .build();
        modalService.replyModal(model.getEvent(), REACTION_REPORT_MODAL_TEMPLATE, modalModel)
                .thenAccept(unused -> {
                    log.info("Created modal for report on message {} from user {} in server {}.",
                        targetMessage.getIdLong(), targetMessage.getAuthor().getIdLong(), targetMessage.getGuild().getIdLong());
                    self.persistModalPayload(targetMessage, modalId, textInputId);
                }).exceptionally(throwable -> {
                    log.error("Failed to create modal for report on message {} from user {} in server {}.",
                            targetMessage.getIdLong(), targetMessage.getAuthor().getIdLong(), targetMessage.getGuild().getIdLong());
                    return null;
                });
        return DefaultListenerResult.PROCESSED;
    }

    @Transactional
    public void persistModalPayload(Message message, String modalId, String inputId) {
        MessageReportModalPayload payload = MessageReportModalPayload
                .builder()
                .channelId(message.getChannel().getIdLong())
                .messageId(message.getIdLong())
                .serverId(message.getGuild().getIdLong())
                .modalId(modalId)
                .textInputId(inputId)
                .build();
        ModalConfigPayload payloadConfig = ModalConfigPayload
                .builder()
                .modalPayload(payload)
                .origin(ReactionReportServiceBean.REACTION_REPORT_MODAL_ORIGIN)
                .payloadType(payload.getClass())
                .modalId(modalId)
                .build();
        componentPayloadManagementService.createModalPayload(payloadConfig, message.getGuild().getIdLong());
    }

    @Override
    public MessageContextConfig getConfig() {
         return MessageContextConfig
                .builder()
                .isTemplated(true)
                 .name("report_message_context")
                .templateKey("report_message_with_context_context_menu_label")
                .build();
    }

    @Override
    public Boolean handlesEvent(MessageContextInteractionModel model) {
        return contextCommandService.matchesGuildContextName(model, getConfig(), model.getServerId());
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.REPORT_REACTIONS;
    }

}
