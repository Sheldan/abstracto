package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageReceivedListener;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.moderation.config.feature.HoneyPotFeatureConfig;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.feature.mode.HoneypotMode;
import dev.sheldan.abstracto.moderation.service.HoneyPotServiceBean;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HoneyPotMessageListener implements AsyncMessageReceivedListener {

    @Autowired
    private ConfigService configService;

    @Autowired
    private HoneyPotServiceBean honeyPotServiceBean;

    @Autowired
    private MessageService messageService;

    @Override
    public DefaultListenerResult execute(MessageReceivedModel model) {
        Long honeyPotChannel = configService.getLongValueOrConfigDefault(HoneyPotFeatureConfig.HONEYPOT_CHANNEL, model.getServerId());
        if(honeyPotChannel == 0) {
            return DefaultListenerResult.IGNORED;
        }
        if(model.getMessage().getChannelId().equals(honeyPotChannel.toString())) {
            boolean honeyPotActivated = honeyPotServiceBean.fellIntoHoneyPotIgnoringJoinDate(model.getServerId(), model.getMessage().getMember());
            if(honeyPotActivated) {
                log.info("Banning user {} in guild {} due to a message in channel {}.", model.getMessage().getAuthor().getIdLong(), model.getServerId(), honeyPotChannel);
                honeyPotServiceBean.banForHoneyPotMessage(model.getMessage().getMember(), honeyPotChannel);
            } else {
                log.info("NOT banning user {} in guild {} due to a message in honeypot channel {}.", model.getMessage().getAuthor().getIdLong(), model.getServerId(), honeyPotChannel);
                messageService.deleteMessage(model.getMessage());
            }
            return DefaultListenerResult.PROCESSED;
        }
        return DefaultListenerResult.IGNORED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.HONEYPOT;
    }

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return List.of(HoneypotMode.MESSAGE);
    }
}
