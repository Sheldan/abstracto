package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.dto.UserDto;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsModel;

import java.util.List;

public interface StarboardService {
    void createStarboardPost(CachedMessage message, List<UserDto> userExceptAuthor, UserInServerDto userReacting, UserInServerDto starredUser);
    void updateStarboardPost(StarboardPost post, CachedMessage message, List<UserDto> userExceptAuthor);
    void removeStarboardPost(StarboardPost message);
    StarStatsModel retrieveStarStats(Long serverId);
}
