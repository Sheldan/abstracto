package dev.sheldan.abstracto.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class ExecutorServiceBean implements ExecutorService {

    @Autowired
    private Environment environment;

    @Value("${abstracto.listener.default.maxPoolSize}")
    private Integer defaultMaxPoolSize;

    @Value("${abstracto.listener.default.corePoolSize}")
    private Integer defaultCorePoolSize;

    @Value("${abstracto.listener.default.keepAliveSeconds}")
    private Integer defaultKeepAliveSeconds;

    private static final String LISTENER_PREFIX = "abstracto.listener.";
    private static final String LISTENER_MAX_POOL_SIZE = "maxPoolSize";
    private static final String LISTENER_CORE_POOL_SIZE = "corePoolSize";
    private static final String LISTENER_KEEP_ALIVE_SECONDS = "keepAliveSeconds";

    private String getPropertyValue(String listenerName, String key,  String defaultValue) {
        return environment.getProperty(LISTENER_PREFIX + listenerName + "." + key, defaultValue);
    }

    private Integer getPropertyValueInteger(String listenerName, String key,  String defaultValue) {
        return Integer.parseInt(getPropertyValue(listenerName, key, defaultValue));
    }

    @Override
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
}
