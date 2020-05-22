package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.exception.SetupStepException;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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


    protected Runnable getTimeoutRunnable(Long serverId, Long channelId) {
        return () -> {
            interactiveUtils.sendTimeoutMessage(serverId, channelId);
        };
    }
}
