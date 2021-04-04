package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.models.template.commands.SetupPostTargetMessageModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Component
@Slf4j
public class PostTargetSetupStep extends AbstractConfigSetupStep {

    public static final String FEATURE_SETUP_POST_TARGET_MESSAGE_TEMPLATE_KEY = "feature_setup_post_target_message";
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
    private ChannelService channelService;

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Override
    public CompletableFuture<SetupStepResult> execute(AServerChannelUserId user, SetupStepParameter parameter) {
        PostTargetStepParameter postTargetStepParameter = (PostTargetStepParameter) parameter;
        TextChannel currentTextChannel;
        if(postTargetManagement.postTargetExists(postTargetStepParameter.getPostTargetKey(), user.getGuildId())) {
            PostTarget postTarget = postTargetManagement.getPostTarget(postTargetStepParameter.getPostTargetKey(), user.getGuildId());
            currentTextChannel = channelService.getTextChannelFromServerOptional(user.getGuildId(), postTarget.getChannelReference().getId()).orElse(null);
        } else {
            currentTextChannel = null;
        }
        SetupPostTargetMessageModel model = SetupPostTargetMessageModel
                .builder()
                .postTargetKey(postTargetStepParameter.getPostTargetKey())
                .currentTextChannel(currentTextChannel)
                .build();
        String messageText = templateService.renderTemplate(FEATURE_SETUP_POST_TARGET_MESSAGE_TEMPLATE_KEY, model, user.getGuildId());
        AChannel channel = channelManagementService.loadChannel(user.getChannelId());
        CompletableFuture<SetupStepResult> future = new CompletableFuture<>();
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(user.getGuildId(), user.getUserId());
        Runnable finalAction = super.getTimeoutRunnable(user.getGuildId(), user.getChannelId());
        log.debug("Executing setup for post target {} in server {} for user {}.", postTargetStepParameter.getPostTargetKey(), user.getGuildId(), user.getUserId());
        Consumer<MessageReceivedEvent> configAction = (MessageReceivedEvent event) -> {
            try {

                SetupStepResult result;
                Message message = event.getMessage();
                if(checkForExit(message)) {
                    log.info("Setup has been cancelled, because of 'exit' message.");
                    result = SetupStepResult.fromCancelled();
                } else {
                    if(message.getMentionedChannels().size() == 0) {
                        log.debug("No mentioned channel was seen in channel, nothing provided.");
                        throw new NoChannelProvidedException();
                    }
                    TextChannel textChannel = message.getMentionedChannels().get(0);
                    PostTargetDelayedActionConfig build = PostTargetDelayedActionConfig
                            .builder()
                            .postTargetKey(postTargetStepParameter.getPostTargetKey())
                            .serverId(user.getGuildId())
                            .textChannel(textChannel)
                            .channelId(textChannel.getIdLong())
                            .build();
                    log.debug("Setup for post target {} in server {} for user {} completed. Storing delayed action.", postTargetStepParameter.getPostTargetKey(), user.getGuildId(), user.getUserId());
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
                future.completeExceptionally(new SetupStepException(e));
            }
        };
        interactiveService.createMessageWithResponse(messageText, aUserInAServer, channel, parameter.getPreviousMessageId(), configAction, finalAction);
        return future;
    }

}
