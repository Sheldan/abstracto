package dev.sheldan.abstracto.twitch.service;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.ITwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwitchServiceConfig {

    @Value("${abstracto.feature.twitch.clientId}")
    private String twitchClientId;

    @Value("${abstracto.feature.twitch.clientSecret}")
    private String twitchSecret;

    @Bean
    public TwitchIdentityProvider twitchIdentityProvider() {
        return new TwitchIdentityProvider(twitchClientId, twitchSecret, null);
    }

    @Bean
    public OAuth2Credential credential(TwitchIdentityProvider tip) {
        return tip.getAppAccessToken();
    }

    @Bean
    public ITwitchClient twitchClient(OAuth2Credential oAuth2Credential) {;
        return TwitchClientBuilder.builder()
                .withClientId(twitchClientId)
                .withClientSecret(twitchSecret)
                .withDefaultAuthToken(oAuth2Credential)
                .withEnableHelix(true)
                .build();
    }

}
