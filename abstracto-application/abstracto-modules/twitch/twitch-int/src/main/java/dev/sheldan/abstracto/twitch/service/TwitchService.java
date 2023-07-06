package dev.sheldan.abstracto.twitch.service;

import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TwitchService {
    User getStreamerByName(String name);
    Optional<User> getStreamerByNameOptional(String name);
    User getStreamerById(String userId);
    List<Stream> getStreamsByUserIds(List<String> userIds);
    Optional<Stream> getStreamOfUser(String userId);
    Map<String, Stream> getStreamsByUserIdsMapped(List<String> userIds);
    void refreshToken();
}
