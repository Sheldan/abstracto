package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InteractiveUtils {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Transactional
    public void sendTimeoutMessage(Long serverId, Long channelId) {
        String s = templateService.renderSimpleTemplate("feature_setup_configuration_timeout", serverId);
        GuildMessageChannel channelOptional = channelService.getMessageChannelFromServer(serverId, channelId);
        channelService.sendTextToChannelNotAsync(s, channelOptional);
    }
}
