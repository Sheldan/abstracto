package dev.sheldan.abstracto.twitch.service;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.ITwitchClient;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.User;
import dev.sheldan.abstracto.twitch.exception.StreamerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TwitchServiceBean implements TwitchService {

    @Autowired
    private ITwitchClient twitchClient;

    @Autowired
    private TwitchIdentityProvider identityProvider;

    @Autowired
    private OAuth2Credential oAuth2Credential;

    @Override
    public User getStreamerByName(String name) {
        return getStreamerByNameOptional(name).orElseThrow(StreamerNotFoundException::new);
    }

    @Override
    public Optional<User> getStreamerByNameOptional(String name) {
        List<User> allUsersWithName = twitchClient.getClientHelper().getTwitchHelix().getUsers(null, null, Collections.singletonList(name)).execute().getUsers();
        if(allUsersWithName.size() != 1) {
            return Optional.empty();
        } else {
            return Optional.of(allUsersWithName.get(0));
        }
    }

    @Override
    public User getStreamerById(String userId) {
        return twitchClient.getClientHelper().getTwitchHelix().getUsers(null, Collections.singletonList(userId), null).execute().getUsers().get(0);
    }

    @Override
    public List<Stream> getStreamsByUserIds(List<String> userIds) {
        return twitchClient.getClientHelper().getTwitchHelix().getStreams(null, null, null, null, null, null, userIds, null).execute().getStreams();
    }

    @Override
    public Optional<Stream> getStreamOfUser(String userId) {
        List<Stream> allStreams = getStreamsByUserIds(Arrays.asList(userId));
        return allStreams.stream().findFirst();
    }

    @Override
    public Map<String, Stream> getStreamsByUserIdsMapped(List<String> userIds) {
        return getStreamsByUserIds(userIds).stream().collect(Collectors.toMap(Stream::getUserId, Function.identity()));
    }

    @Override
    public void refreshToken() {
        oAuth2Credential.updateCredential(identityProvider.getAppAccessToken());
    }
}
