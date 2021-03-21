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

    @Bean(name = "joinListenerExecutor")
    public TaskExecutor joinListenerExecutor() {
        return executorService.setupExecutorFor("joinListener");
    }

    @Bean(name = "leaveListenerExecutor")
    public TaskExecutor leaveListenerExecutor() {
        return executorService.setupExecutorFor("leaveListener");
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

   

}
