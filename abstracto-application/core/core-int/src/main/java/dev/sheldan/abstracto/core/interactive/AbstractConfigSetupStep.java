package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

@Getter
@Setter
public abstract class AbstractConfigSetupStep implements SetupStep {

    @Autowired
    private InteractiveService interactiveService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    protected SetupExecution nextStep;

    @Autowired
    private InteractiveUtils interactiveUtils;


    protected Consumer<MessageReceivedModel> getTimeoutConsumer(Long serverId, Long channelId) {
        return (messageReceivedModel) -> interactiveUtils.sendTimeoutMessage(serverId, channelId);
    }

    protected boolean checkForExit(Message message) {
        return message.getContentRaw().trim().equalsIgnoreCase("exit");
    }

    protected boolean checkForKeep(Message message) {
        return message.getContentRaw().trim().equalsIgnoreCase("default");
    }
}
