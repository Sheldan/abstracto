package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Component
@Slf4j
public class SystemConfigSetupStep extends AbstractConfigSetupStep {

    @Autowired
    private ConfigService configService;

    @Autowired
    private InteractiveService interactiveService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private SystemConfigSetupStep self;

    @Override
    public CompletableFuture<List<DelayedActionConfig>> execute(AServerChannelUserId user, SetupStepParameter parameter) {
        SystemConfigStepParameter systemConfigStepParameter = (SystemConfigStepParameter) parameter;
        String messageTemplateKey = "setup_config_" + systemConfigStepParameter.getConfigKey();
        Optional<AChannel> channel = channelManagementService.loadChannel(user.getChannelId());
        CompletableFuture<List<DelayedActionConfig>> future = new CompletableFuture<>();
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(user.getGuildId(), user.getUserId());
        if(channel.isPresent()) {
            Runnable finalAction = super.getTimeoutRunnable(user.getGuildId(), user.getChannelId());
            Consumer<MessageReceivedEvent> configAction = (MessageReceivedEvent event) -> {
                try {
                    self.checkValidity(user, systemConfigStepParameter, event);
                    SystemConfigDelayedActionConfig build = SystemConfigDelayedActionConfig
                            .builder()
                            .configKey(systemConfigStepParameter.getConfigKey())
                            .serverId(user.getGuildId())
                            .value(event.getMessage().getContentRaw())
                            .build();
                    List<DelayedActionConfig> delayedSteps = Arrays.asList(build);
                    future.complete(delayedSteps);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            };
            interactiveService.createMessageWithResponse(messageTemplateKey, aUserInAServer, channel.get(), parameter.getPreviousMessageId(), configAction, finalAction);
        } else {
            future.completeExceptionally(new ChannelNotFoundException(user.getGuildId(), user.getChannelId()));
        }
        return future;
    }

    @Transactional
    public void checkValidity(AServerChannelUserId user, SystemConfigStepParameter systemConfigStepParameter, MessageReceivedEvent event) {
        configService.validateConfig(systemConfigStepParameter.getConfigKey(), user.getGuildId(), event.getMessage().getContentRaw());
    }


}
