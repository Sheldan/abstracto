package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncUserUnBannedListener;
import dev.sheldan.abstracto.core.models.listener.UserUnBannedModel;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import dev.sheldan.abstracto.moderation.model.template.listener.UserUnBannedListenerLogModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserUnBannedListener implements AsyncUserUnBannedListener {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    private static final String USER_UN_BANNED_NOTIFICATION_TEMPLATE = "userUnBanned_listener_notification";

    @Override
    public DefaultListenerResult execute(UserUnBannedModel eventModel) {
        log.info("Notifying about unban of user {} in guild {}.", eventModel.getUnBannedServerUser().getUserId(), eventModel.getServerId());
        UserUnBannedListenerLogModel model = UserUnBannedListenerLogModel
                .builder()
                .unBannedUser(eventModel.getUnBannedUser() != null ? UserDisplay.fromUser(eventModel.getUnBannedUser()) : UserDisplay.fromId(eventModel.getUnBannedServerUser().getUserId()))
                .unBanningUser(eventModel.getUnBanningUser() != null ? UserDisplay.fromUser(eventModel.getUnBanningUser()) : UserDisplay.fromServerUser(eventModel.getUnBanningServerUser()))
                .reason(eventModel.getReason())
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(USER_UN_BANNED_NOTIFICATION_TEMPLATE, model, eventModel.getServerId());
        FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(messageToSend, ModerationPostTarget.BAN_LOG, eventModel.getServerId()));
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }
}
