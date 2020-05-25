package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.models.template.commands.SetupPostTargetMessageModel;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Component
@Slf4j
public class PostTargetSetupStep extends AbstractConfigSetupStep {

    @Autowired
    private ConfigService configService;

    @Autowired
    private InteractiveService interactiveService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private PostTargetSetupStep self;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private BotService botService;

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Override
    public CompletableFuture<SetupStepResult> execute(AServerChannelUserId user, SetupStepParameter parameter) {
        PostTargetStepParameter postTargetStepParameter = (PostTargetStepParameter) parameter;
        TextChannel currentTextChannel;
        if(postTargetManagement.postTargetExists(postTargetStepParameter.getPostTargetKey(), user.getGuildId())) {
            PostTarget postTarget = postTargetManagement.getPostTarget(postTargetStepParameter.getPostTargetKey(), user.getGuildId());
            currentTextChannel = botService.getTextChannelFromServer(user.getGuildId(), postTarget.getChannelReference().getId()).orElse(null);
        } else {
            currentTextChannel = null;
        }
        SetupPostTargetMessageModel model = SetupPostTargetMessageModel
                .builder()
                .postTargetKey(postTargetStepParameter.getPostTargetKey())
                .currentTextChannel(currentTextChannel)
                .build();
        String messageTemplateKey = "setup_post_target_message";
        String messageText = templateService.renderTemplate(messageTemplateKey, model);
        Optional<AChannel> channel = channelManagementService.loadChannel(user.getChannelId());
        CompletableFuture<SetupStepResult> future = new CompletableFuture<>();
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(user.getGuildId(), user.getUserId());
        if(channel.isPresent()) {
            Runnable finalAction = super.getTimeoutRunnable(user.getGuildId(), user.getChannelId());
            Consumer<MessageReceivedEvent> configAction = (MessageReceivedEvent event) -> {
                try {

                    SetupStepResult result;
                    Message message = event.getMessage();
                    if(checkForExit(message)) {
                        result = SetupStepResult.fromCancelled();
                    } else {
                        if(message.getMentionedChannels().size() == 0) {
                            future.completeExceptionally(new RuntimeException());
                            return;
                        }
                        TextChannel textChannel = message.getMentionedChannels().get(0);
                        PostTargetDelayedActionConfig build = PostTargetDelayedActionConfig
                                .builder()
                                .postTargetKey(postTargetStepParameter.getPostTargetKey())
                                .serverId(user.getGuildId())
                                .textChannel(textChannel)
                                .channelId(textChannel.getIdLong())
                                .build();
                        List<DelayedActionConfig> delayedSteps = Arrays.asList(build);
                        result = SetupStepResult
                                .builder()
                                .result(SetupStepResultType.SUCCESS)
                                .delayedActionConfigList(delayedSteps)
                                .build();
                    }

                    future.complete(result);
                } catch (Exception e) {
                    log.error("Failed to handle post target step.", e);
                    future.completeExceptionally(e);
                }
            };
            interactiveService.createMessageWithResponse(messageText, aUserInAServer, channel.get(), parameter.getPreviousMessageId(), configAction, finalAction);
        } else {
            future.completeExceptionally(new ChannelNotFoundException(user.getGuildId(), user.getChannelId()));
        }
        return future;
    }

}
