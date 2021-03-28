package dev.sheldan.abstracto.core.config;

import ch.qos.logback.core.net.ssl.SecureRandomFactoryBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import dev.sheldan.abstracto.core.metric.OkHttpMetrics;
import dev.sheldan.abstracto.core.service.BotService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class CoreConfig {

    @Autowired
    private BotService botService;

    @Value("${abstracto.eventWaiter.threads}")
    private Integer threadCount;

    @Autowired
    private OkHttpMetrics okHttpMetrics;

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
                .registerTypeAdapter(Instant.class, new InstantTimeAdapter())
                .setPrettyPrinting().create();
    }


    @Bean
    public EventWaiter eventWaiter() {
        ScheduledExecutorService scheduledExecutorService =
                Executors.newScheduledThreadPool(threadCount);
        return new EventWaiter(scheduledExecutorService, true);
    }

    @Bean
    public OkHttpClient client() {
        return new OkHttpClient.Builder().addInterceptor(okHttpMetrics).build();
    }

    @Bean
    public SecureRandom secureRandom() throws NoSuchProviderException, NoSuchAlgorithmException {
        return new SecureRandomFactoryBean().createSecureRandom();
    }
}
