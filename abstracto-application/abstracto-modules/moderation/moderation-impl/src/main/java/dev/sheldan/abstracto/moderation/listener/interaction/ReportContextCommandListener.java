package dev.sheldan.abstracto.moderation.listener.interaction;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.MessageContextConfig;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.interaction.context.message.listener.MessageContextCommandListener;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.interaction.MessageContextInteractionModel;
import dev.sheldan.abstracto.core.interaction.context.ContextCommandService;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.service.ReactionReportService;
import dev.sheldan.abstracto.moderation.service.ReactionReportServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static dev.sheldan.abstracto.moderation.service.ReactionReportServiceBean.REACTION_REPORT_RESPONSE_TEMPLATE;

@Component
@Slf4j
public class ReportContextCommandListener implements MessageContextCommandListener {

    @Autowired
    private ContextCommandService contextCommandService;

    @Autowired
    private ReactionReportService reactionReportService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public DefaultListenerResult execute(MessageContextInteractionModel model) {
        model.getEvent().deferReply(true).queue(interactionHook -> {
            Message targetMessage = model.getEvent().getTarget();
            if(targetMessage.getAuthor().getIdLong() == model.getEvent().getUser().getIdLong()) {
                interactionService.sendMessageToInteraction(ReactionReportServiceBean.REACTION_REPORT_OWN_MESSAGE_RESPONSE_TEMPLATE, new Object(), interactionHook);
                return;
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
                interactionService.sendMessageToInteraction(ReactionReportServiceBean.REACTION_REPORT_COOLDOWN_RESPONSE_TEMPLATE, new Object(),interactionHook);
                return;
            }
            reactionReportService.createReactionReport(targetMessage, userReporting, null).exceptionally(throwable -> {
                log.error("Failed to create reaction report in server {} on message {} in channel {} with interaction.",
                        model.getServerId(), targetMessage.getIdLong(), model.getEvent().getChannel().getIdLong(), throwable);
                return null;
            });
            interactionService.sendMessageToInteraction(REACTION_REPORT_RESPONSE_TEMPLATE, new Object(), interactionHook);
        });

        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public MessageContextConfig getConfig() {
         return MessageContextConfig
                .builder()
                .isTemplated(true)
                 .name("report_message")
                .templateKey("report_message_context_menu_label")
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
