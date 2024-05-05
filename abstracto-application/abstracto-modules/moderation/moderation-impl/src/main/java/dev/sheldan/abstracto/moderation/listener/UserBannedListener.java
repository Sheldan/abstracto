package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncUserBannedListener;
import dev.sheldan.abstracto.core.models.listener.UserBannedListenerModel;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.template.listener.UserBannedLogModel;
import dev.sheldan.abstracto.moderation.service.BanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserBannedListener implements AsyncUserBannedListener {

    @Autowired
    private BanService banService;

    @Override
    public DefaultListenerResult execute(UserBannedListenerModel eventModel) {
        log.info("Notifying about ban of user {} in guild {}.", eventModel.getBannedServerUser().getUserId(), eventModel.getServerId());
        if(eventModel.getBanningServerUser().getUserId() == eventModel.getGuild().getJDA().getSelfUser().getIdLong()) {
            log.info("Skipping logging banned event about user {} in guild {}, because it was done by us.", eventModel.getBannedServerUser().getUserId(), eventModel.getGuild().getIdLong());
            return DefaultListenerResult.IGNORED;
        }
        UserBannedLogModel model = UserBannedLogModel
                .builder()
                .bannedUser(eventModel.getBannedUser() != null ? UserDisplay.fromUser(eventModel.getBannedUser()) : UserDisplay.fromId(eventModel.getBannedServerUser().getUserId()))
                .banningUser(eventModel.getBanningUser() != null ? UserDisplay.fromUser(eventModel.getBanningUser()) : UserDisplay.fromServerUser(eventModel.getBanningServerUser()))
                .reason(eventModel.getReason())
                .build();
        banService.sendBanLogMessage(model, eventModel.getServerId());
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }
}
