package dev.sheldan.abstracto.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ListenerExecutorConfig {

    @Value("${abstracto.listener.default.maxPoolSize}")
    private Integer defaultMaxPoolSize;

    @Value("${abstracto.listener.default.corePoolSize}")
    private Integer defaultCorePoolSize;

    @Value("${abstracto.listener.default.keepAliveSeconds}")
    private Integer defaultKeepAliveSeconds;

    @Autowired
    private Environment environment;

    private static final String LISTENER_PREFIX = "abstracto.listener.";
    private static final String LISTENER_MAX_POOL_SIZE = "maxPoolSize";
    private static final String LISTENER_CORE_POOL_SIZE = "corePoolSize";
    private static final String LISTENER_KEEP_ALIVE_SECONDS = "keepAliveSeconds";

    @Bean(name = "joinListenerExecutor")
    public TaskExecutor joinListenerExecutor() {
        return setupExecutorFor("joinListener");
    }

    @Bean(name = "leaveListenerExecutor")
    public TaskExecutor leaveListenerExecutor() {
        return setupExecutorFor("leaveListener");
    }

    @Bean(name = "messageReceivedExecutor")
    public TaskExecutor messageReceivedExecutor() {
        return setupExecutorFor("messageReceivedListener");
    }

    @Bean(name = "messageDeletedExecutor")
    public TaskExecutor messageDeletedExecutor() {
        return setupExecutorFor("messageReceivedListener");
    }

    @Bean(name = "messageEmbeddedExecutor")
    public TaskExecutor messageEmbeddedExecutor() {
        return setupExecutorFor("messageEmbeddedListener");
    }

    @Bean(name = "messageUpdatedExecutor")
    public TaskExecutor messageUpdatedExecutor() {
        return setupExecutorFor("messageUpdatedListener");
    }

    @Bean(name = "privateMessageReceivedExecutor")
    public TaskExecutor privateMessageReceivedExecutor() {
        return setupExecutorFor("privateMessageReceivedListener");
    }

    @Bean(name = "emoteCreatedExecutor")
    public TaskExecutor emoteCreatedExecutor() {
        return setupExecutorFor("emoteCreatedListener");
    }

    @Bean(name = "emoteDeletedExecutor")
    public TaskExecutor emoteDeletedExecutor() {
        return setupExecutorFor("emoteDeletedListener");
    }

    @Bean(name = "emoteUpdatedExecutor")
    public TaskExecutor emoteUpdatedExecutor() {
        return setupExecutorFor("emoteUpdatedListener");
    }

    @Bean(name = "reactionAddedExecutor")
    public TaskExecutor reactionAddedExecutor() {
        return setupExecutorFor("reactionAddedListener");
    }

    @Bean(name = "reactionRemovedExecutor")
    public TaskExecutor reactionRemovedExecutor() {
        return setupExecutorFor("reactionRemovedListener");
    }

    @Bean(name = "reactionClearedExecutor")
    public TaskExecutor reactionClearedExecutor() {
        return setupExecutorFor("reactionClearedListener");
    }

    public ThreadPoolTaskExecutor setupExecutorFor(String listenerName) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        Integer maxPoolSize = getPropertyValueInteger(listenerName, LISTENER_MAX_POOL_SIZE, defaultMaxPoolSize.toString());
        Integer corePoolSize = getPropertyValueInteger(listenerName, LISTENER_CORE_POOL_SIZE, defaultCorePoolSize.toString());
        Integer keepAliveSeconds = getPropertyValueInteger(listenerName, LISTENER_KEEP_ALIVE_SECONDS, defaultKeepAliveSeconds.toString());
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(listenerName + "-task-executor-thread");
        executor.initialize();
        return executor;
    }

    public String getPropertyValue(String listenerName, String key,  String defaultValue) {
        return environment.getProperty(LISTENER_PREFIX + listenerName + "." + key, defaultValue);
    }

    public Integer getPropertyValueInteger(String listenerName, String key,  String defaultValue) {
        return Integer.parseInt(getPropertyValue(listenerName, key, defaultValue));
    }

}
