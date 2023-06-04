package dev.sheldan.abstracto.core.config;

import dev.sheldan.abstracto.core.service.ExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class ListenerExecutorConfig {
    
    @Autowired
    private ExecutorService executorService;

    @Bean(name = "memberTimeoutUpdatedListenerExecutor")
    public TaskExecutor memberTimeoutUpdatedListenerExecutor() {
        return executorService.setupExecutorFor("memberTimeoutListener");
    }

    @Bean(name = "joinListenerExecutor")
    public TaskExecutor joinListenerExecutor() {
        return executorService.setupExecutorFor("joinListener");
    }

    @Bean(name = "memberPendingUpdateExecutor")
    public TaskExecutor memberPendingUpdateExecutor() {
        return executorService.setupExecutorFor("memberPendingUpdateListener");
    }

    @Bean(name = "leaveListenerExecutor")
    public TaskExecutor leaveListenerExecutor() {
        return executorService.setupExecutorFor("leaveListener");
    }

    @Bean(name = "boostTimeUpdateExecutor")
    public TaskExecutor boostTimeUpdateExecutor() {
        return executorService.setupExecutorFor("boostTimeUpdateListener");
    }

    @Bean(name = "messageReceivedExecutor")
    public TaskExecutor messageReceivedExecutor() {
        return executorService.setupExecutorFor("messageReceivedListener");
    }

    @Bean(name = "messageDeletedExecutor")
    public TaskExecutor messageDeletedExecutor() {
        return executorService.setupExecutorFor("messageReceivedListener");
    }

    @Bean(name = "messageEmbeddedExecutor")
    public TaskExecutor messageEmbeddedExecutor() {
        return executorService.setupExecutorFor("messageEmbeddedListener");
    }

    @Bean(name = "messageUpdatedExecutor")
    public TaskExecutor messageUpdatedExecutor() {
        return executorService.setupExecutorFor("messageUpdatedListener");
    }

    @Bean(name = "privateMessageReceivedExecutor")
    public TaskExecutor privateMessageReceivedExecutor() {
        return executorService.setupExecutorFor("privateMessageReceivedListener");
    }

    @Bean(name = "emoteCreatedExecutor")
    public TaskExecutor emoteCreatedExecutor() {
        return executorService.setupExecutorFor("emoteCreatedListener");
    }

    @Bean(name = "buttonClickedExecutor")
    public TaskExecutor buttonClickedExecutor() {
        return executorService.setupExecutorFor("buttonClickedListener");
    }

    @Bean(name = "stringSelectMenuExecutor")
    public TaskExecutor stringSelectMenuExecutor() {
        return executorService.setupExecutorFor("stringSelectMenuListener");
    }

    @Bean(name = "modalInteractionExecutor")
    public TaskExecutor modalInteractionExecutor() {
        return executorService.setupExecutorFor("modalInteractionListener");
    }

    @Bean(name = "messageContextCommandExecutor")
    public TaskExecutor messageContextCommandExecutor() {
        return executorService.setupExecutorFor("messageContextCommandListener");
    }

    @Bean(name = "slashCommandExecutor")
    public TaskExecutor slashCommandExecutor() {
        return executorService.setupExecutorFor("slashCommandListener");
    }

    @Bean(name = "slashCommandAutoCompleteExecutor")
    public TaskExecutor slashCommandAutoCompleteExecutor() {
        return executorService.setupExecutorFor("slashCommandAutoCompleteListener");
    }

    @Bean(name = "emoteDeletedExecutor")
    public TaskExecutor emoteDeletedExecutor() {
        return executorService.setupExecutorFor("emoteDeletedListener");
    }

    @Bean(name = "emoteUpdatedExecutor")
    public TaskExecutor emoteUpdatedExecutor() {
        return executorService.setupExecutorFor("emoteUpdatedListener");
    }

    @Bean(name = "reactionAddedExecutor")
    public TaskExecutor reactionAddedExecutor() {
        return executorService.setupExecutorFor("reactionAddedListener");
    }

    @Bean(name = "reactionRemovedExecutor")
    public TaskExecutor reactionRemovedExecutor() {
        return executorService.setupExecutorFor("reactionRemovedListener");
    }

    @Bean(name = "reactionClearedExecutor")
    public TaskExecutor reactionClearedExecutor() {
        return executorService.setupExecutorFor("reactionClearedListener");
    }

    @Bean(name = "aChannelCreatedExecutor")
    public TaskExecutor aChannelCreatedExecutor() {
        return executorService.setupExecutorFor("aChannelCreatedListener");
    }

    @Bean(name = "cacheClearedExecutor")
    public TaskExecutor cacheClearedExecutor() {
        return executorService.setupExecutorFor("cacheClearedListener");
    }

    @Bean(name = "aChannelDeletedExecutor")
    public TaskExecutor aChannelDeletedExecutor() {
        return executorService.setupExecutorFor("aChannelDeletedListener");
    }

    @Bean(name = "aRoleCreatedExecutor")
    public TaskExecutor aRoleCreatedExecutor() {
        return executorService.setupExecutorFor("aRoleCreatedListener");
    }

    @Bean(name = "aRoleDeletedExecutor")
    public TaskExecutor aRoleDeletedExecutor() {
        return executorService.setupExecutorFor("aRoleDeletedListener");
    }

    @Bean(name = "channelGroupCreatedExecutor")
    public TaskExecutor channelGroupCreatedExecutor() {
        return executorService.setupExecutorFor("channelGroupCreatedListener");
    }

    @Bean(name = "featureActivationExecutor")
    public TaskExecutor featureActivationListener() {
        return executorService.setupExecutorFor("featureActivationListener");
    }

    @Bean(name = "featureDeactivationExecutor")
    public TaskExecutor featureDeactivationListener() {
        return executorService.setupExecutorFor("featureDeactivationListener");
    }

    @Bean(name = "serverJoinExecutor")
    public TaskExecutor serverJoinExecutor() {
        return executorService.setupExecutorFor("serverJoinListener");
    }

    @Bean(name = "roleCreatedExecutor")
    public TaskExecutor roleCreatedExecutor() {
        return executorService.setupExecutorFor("roleCreatedListener");
    }

    @Bean(name = "roleDeletedExecutor")
    public TaskExecutor roleDeletedExecutor() {
        return executorService.setupExecutorFor("roleDeletedListener");
    }

    @Bean(name = "channelCreatedExecutor")
    public TaskExecutor channelCreatedExecutor() {
        return executorService.setupExecutorFor("channelCreatedListener");
    }

    @Bean(name = "channelDeletedExecutor")
    public TaskExecutor channelDeletedExecutor() {
        return executorService.setupExecutorFor("channelDeletedListener");
    }

    @Bean(name = "userUnBannedListenerExecutor")
    public TaskExecutor userUnBannedListenerExecutor() {
        return executorService.setupExecutorFor("userUnBannedListener");
    }

    @Bean(name = "userBannedListenerExecutor")
    public TaskExecutor userBannedListenerExecutor() {
        return executorService.setupExecutorFor("userBannedListener");
    }

    @Bean(name = "voiceChatJoinedExecutor")
    public TaskExecutor voiceChatJoinedExecutor() {
        return executorService.setupExecutorFor("voiceChatJoinedListener");
    }

    @Bean(name = "voiceChatLeftExecutor")
    public TaskExecutor voiceChatLeftExecutor() {
        return executorService.setupExecutorFor("voiceChatLeftListener");
    }

}
