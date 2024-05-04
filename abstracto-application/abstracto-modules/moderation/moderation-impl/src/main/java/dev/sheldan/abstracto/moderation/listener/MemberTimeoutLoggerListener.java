package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMemberTimeoutUpdatedListener;
import dev.sheldan.abstracto.core.models.listener.MemberTimeoutUpdatedModel;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.posttarget.MutingPostTarget;
import dev.sheldan.abstracto.moderation.model.template.command.MuteListenerModel;
import dev.sheldan.abstracto.moderation.service.MuteServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
public class MemberTimeoutLoggerListener implements AsyncMemberTimeoutUpdatedListener {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public DefaultListenerResult execute(MemberTimeoutUpdatedModel model) {
        Guild guild = model.getGuild();
        MemberDisplay memberDisplay = model.getMember() != null ? MemberDisplay.fromMember(model.getMember()) : MemberDisplay.fromServerUser(model.getTimeoutUser());
        Duration duration = null;
        if(model.getNewTimeout() != null) {
            duration = Duration.between(Instant.now(), model.getNewTimeout());
        }
        MuteListenerModel muteLogModel = MuteListenerModel
                .builder()
                .muteTargetDate(model.getNewTimeout() != null ? model.getNewTimeout().toInstant() : null)
                .oldMuteTargetDate(model.getOldTimeout() != null ? model.getOldTimeout().toInstant() : null)
                .mutingUser(MemberDisplay.fromIds(model.getServerId(), model.getResponsibleUserId()))
                .mutedUser(memberDisplay)
                .duration(duration)
                .reason(model.getReason())
                .build();
        MessageToSend message = templateService.renderEmbedTemplate(MuteServiceBean.MUTE_LOG_TEMPLATE, muteLogModel, guild.getIdLong());
        FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(message, MutingPostTarget.MUTE_LOG, model.getServerId()));
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MUTING;
    }
}
