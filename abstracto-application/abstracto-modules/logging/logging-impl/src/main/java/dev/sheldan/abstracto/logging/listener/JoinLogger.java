package dev.sheldan.abstracto.logging.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncJoinListener;
import dev.sheldan.abstracto.core.models.listener.MemberJoinModel;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.logging.config.LoggingFeatureDefinition;
import dev.sheldan.abstracto.logging.config.LoggingPostTarget;
import dev.sheldan.abstracto.logging.model.template.MemberJoinLogModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class JoinLogger implements AsyncJoinListener {

    public static final String USER_JOIN_TEMPLATE = "user_join";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public DefaultListenerResult execute(MemberJoinModel listenerModel) {
        MemberJoinLogModel model = MemberJoinLogModel
                .builder()
                .member(listenerModel.getMember())
                .build();
        log.debug("Logging join event for user {} in server {}.", listenerModel.getMember().getIdLong(), listenerModel.getServerId());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(USER_JOIN_TEMPLATE, model, listenerModel.getServerId());
        FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(messageToSend, LoggingPostTarget.JOIN_LOG, listenerModel.getServerId()))
        .exceptionally(throwable -> {
            log.error("Failed to send member joining log.", throwable);
            return null;
        });
        return DefaultListenerResult.PROCESSED;
    }


    @Override
    public FeatureDefinition getFeature() {
        return LoggingFeatureDefinition.LOGGING;
    }

}
