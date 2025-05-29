package dev.sheldan.abstracto.core.config;

import ch.qos.logback.core.net.ssl.SecureRandomFactoryBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import dev.sheldan.abstracto.core.logging.OkHttpLogger;
import dev.sheldan.abstracto.core.metric.OkHttpMetrics;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.ActionRowButtonConfig;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.ActionRowItemConfig;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.ComponentConfig;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.SectionAccessoryConfig;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.SectionButton;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.SectionComponentConfig;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.SectionTextDisplay;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.SectionThumbnail;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.TopLevelActionRowConfig;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.TopLevelContainerConfig;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.TopLevelFileConfig;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.TopLevelMediaGalleryConfig;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.TopLevelSectionConfig;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.TopLevelSeperatorConfig;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.TopLevelTextDisplay;
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
                .registerTypeAdapterFactory(RuntimeTypeAdapterFactory
                    .of(ComponentConfig.class, "type")
                    .registerSubtype(TopLevelActionRowConfig.class, "actionRow")
                    .registerSubtype(TopLevelSectionConfig.class, "section")
                    .registerSubtype(TopLevelFileConfig.class, "fileDisplay")
                    .registerSubtype(TopLevelMediaGalleryConfig.class, "mediaGallery")
                    .registerSubtype(TopLevelSeperatorConfig.class, "separator")
                    .registerSubtype(TopLevelContainerConfig.class, "container")
                    .registerSubtype(TopLevelTextDisplay.class, "textDisplay"))
                .registerTypeAdapterFactory(RuntimeTypeAdapterFactory
                    .of(ActionRowItemConfig.class, "type")
                    .registerSubtype(ActionRowButtonConfig.class, "button"))
                .registerTypeAdapterFactory(RuntimeTypeAdapterFactory
                    .of(SectionAccessoryConfig.class, "type")
                    .registerSubtype(SectionButton.class, "button")
                    .registerSubtype(SectionThumbnail.class, "thumbnail")
                )
                .registerTypeAdapterFactory(RuntimeTypeAdapterFactory
                    .of(SectionComponentConfig.class, "type")
                    .registerSubtype(SectionTextDisplay.class, "textDisplay")
                )
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
