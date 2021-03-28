package dev.sheldan.abstracto.webservices.youtube.config;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YoutubeServiceBeans {

    @Value("${abstracto.feature.youtube.apiKey}")
    private String youtubeApiKey;

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public YouTube getYouTubeBean() {
        return new YouTube.Builder(getHttpTransport(), getJsonFactory(), request -> {}).setApplicationName(applicationName)
          .setYouTubeRequestInitializer(new YouTubeRequestInitializer(youtubeApiKey)).build();
    }


    @Bean
    public HttpTransport getHttpTransport() {
        return new NetHttpTransport();
    }

    @Bean
    public JsonFactory getJsonFactory() {
        return new GsonFactory();
    }

}
