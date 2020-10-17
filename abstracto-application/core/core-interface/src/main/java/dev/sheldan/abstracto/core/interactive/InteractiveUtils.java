package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class InteractiveUtils {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Transactional
    public void sendTimeoutMessage(Long serverId, Long channelId) {
        String s = templateService.renderSimpleTemplate("feature_setup_configuration_timeout");
        Optional<TextChannel> channelOptional = channelService.getTextChannelInGuild(serverId, channelId);
        channelOptional.ifPresent(channel -> channelService.sendTextToChannelNotAsync(s, channel));
    }
}
