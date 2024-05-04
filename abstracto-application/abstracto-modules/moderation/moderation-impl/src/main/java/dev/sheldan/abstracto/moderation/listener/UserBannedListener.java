package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncUserBannedListener;
import dev.sheldan.abstracto.core.models.listener.UserBannedModel;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import dev.sheldan.abstracto.moderation.model.template.listener.UserBannedListenerLogModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserBannedListener implements AsyncUserBannedListener {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    public static final String USER_BANNED_NOTIFICATION_TEMPLATE = "userBanned_listener_notification";

    @Override
    public DefaultListenerResult execute(UserBannedModel eventModel) {
        log.info("Notifying about ban of user {} in guild {}.", eventModel.getBannedServerUser().getUserId(), eventModel.getServerId());
        UserBannedListenerLogModel model = UserBannedListenerLogModel
                .builder()
                .bannedUser(eventModel.getBannedUser() != null ? UserDisplay.fromUser(eventModel.getBannedUser()) : UserDisplay.fromId(eventModel.getBannedServerUser().getUserId()))
                .banningUser(eventModel.getBanningUser() != null ? UserDisplay.fromUser(eventModel.getBanningUser()) : UserDisplay.fromServerUser(eventModel.getBanningServerUser()))
                .reason(eventModel.getReason())
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(USER_BANNED_NOTIFICATION_TEMPLATE, model, eventModel.getServerId());
        FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(messageToSend, ModerationPostTarget.BAN_LOG, eventModel.getServerId()));
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }
}
