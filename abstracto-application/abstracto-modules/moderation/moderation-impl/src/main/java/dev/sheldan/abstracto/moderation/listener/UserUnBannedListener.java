package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncUserUnBannedListener;
import dev.sheldan.abstracto.core.models.listener.UserUnBannedListenerModel;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.template.listener.UserUnBannedLogModel;
import dev.sheldan.abstracto.moderation.service.BanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserUnBannedListener implements AsyncUserUnBannedListener {

    @Autowired
    private BanService banService;

    @Override
    public DefaultListenerResult execute(UserUnBannedListenerModel eventModel) {
        log.info("Notifying about unban of user {} in guild {}.", eventModel.getUnBannedServerUser().getUserId(), eventModel.getServerId());
        if(eventModel.getUnBanningUser().getIdLong() == eventModel.getGuild().getSelfMember().getIdLong()) {
            log.info("Skipping logging banned event about user {} in guild {}, because it was done by us.", eventModel.getUnBannedServerUser().getUserId(), eventModel.getGuild().getIdLong());
            return DefaultListenerResult.IGNORED;
        }
        UserUnBannedLogModel model = UserUnBannedLogModel
                .builder()
                .unBannedUser(eventModel.getUnBannedUser() != null ? UserDisplay.fromUser(eventModel.getUnBannedUser()) : UserDisplay.fromId(eventModel.getUnBannedServerUser().getUserId()))
                .unBanningUser(eventModel.getUnBanningUser() != null ? UserDisplay.fromUser(eventModel.getUnBanningUser()) : UserDisplay.fromServerUser(eventModel.getUnBanningServerUser()))
                .reason(eventModel.getReason())
                .build();
        banService.sendUnBanLogMessage(model, eventModel.getServerId());
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }
}
