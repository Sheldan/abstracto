package dev.sheldan.abstracto.logging.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncLeaveListener;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.MemberLeaveModel;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.logging.config.LoggingFeatureDefinition;
import dev.sheldan.abstracto.logging.config.LoggingPostTarget;
import dev.sheldan.abstracto.logging.model.template.MemberLeaveLogModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LeaveLogger implements AsyncLeaveListener {

    public static final String USER_LEAVE_TEMPLATE = "user_leave";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public FeatureDefinition getFeature() {
        return LoggingFeatureDefinition.LOGGING;
    }

    @Override
    public DefaultListenerResult execute(MemberLeaveModel listenerModel) {
        ServerUser leavingUser = ServerUser
                .builder()
                .userId(listenerModel.getUser().getIdLong())
                .serverId(listenerModel.getServerId())
                .build();
        MemberLeaveLogModel model = MemberLeaveLogModel
                .builder()
                .leavingUser(leavingUser)
                .user(UserDisplay.fromUser(listenerModel.getUser()))
                .build();
        log.debug("Logging leave event for user {} in server {}.", listenerModel.getUser().getIdLong(), listenerModel.getServerId());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(USER_LEAVE_TEMPLATE, model, listenerModel.getServerId());
        FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(messageToSend, LoggingPostTarget.LEAVE_LOG, listenerModel.getServerId()))
        .exceptionally(throwable -> {
            log.error("Failed to send member leaving log.", throwable);
            return null;
        });
        return DefaultListenerResult.PROCESSED;
    }
}
