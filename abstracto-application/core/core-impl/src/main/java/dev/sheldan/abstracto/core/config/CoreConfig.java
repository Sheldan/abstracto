package dev.sheldan.abstracto.core.config;

import ch.qos.logback.core.net.ssl.SecureRandomFactoryBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.sheldan.abstracto.core.logging.OkHttpLogger;
import dev.sheldan.abstracto.core.metric.OkHttpMetrics;
import dev.sheldan.abstracto.core.service.BotService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Configuration
public class CoreConfig {

    @Autowired
    private BotService botService;

    @Autowired
    private OkHttpMetrics okHttpMetrics;

    @Autowired
    private OkHttpLogger okHttpLogger;

    @Autowired
    private List<CustomJsonSerializer> customJsonSerializers;

    @Autowired
    private List<CustomJsonDeSerializer> customJsonDeSerializers;

    @Bean
    public Gson gson() {
        GsonBuilder builder = new GsonBuilder()
                .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .setPrettyPrinting();
        if(customJsonDeSerializers != null) {
            customJsonDeSerializers.forEach(customJsonSerializer ->
                    builder.registerTypeAdapter(customJsonSerializer.getType(), customJsonSerializer));
        }
        if(customJsonSerializers != null) {
            customJsonSerializers.forEach(customJsonSerializer ->
                    builder.registerTypeAdapter(customJsonSerializer.getType(), customJsonSerializer));
        }
        return builder.create();
    }

    @Bean
    public OkHttpClient client() {
        return new OkHttpClient.Builder()
                .addInterceptor(okHttpMetrics)
                .addInterceptor(okHttpLogger)
                .build();
    }

    @Bean
    public SecureRandom secureRandom() throws NoSuchProviderException, NoSuchAlgorithmException {
        return new SecureRandomFactoryBean().createSecureRandom();
    }
}
