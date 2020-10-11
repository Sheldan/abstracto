package dev.sheldan.abstracto.utility.repository.converter;

import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsUser;
import dev.sheldan.abstracto.utility.repository.StarStatsUserResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class StarStatsUserConverter {

    @Autowired
    private BotService botService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    public List<CompletableFuture<StarStatsUser>> convertToStarStatsUser(List<StarStatsUserResult> users, Long serverId) {
        List<CompletableFuture<StarStatsUser>> result = new ArrayList<>();
        users.forEach(starStatsUserResult ->
            result.add(createStarStatsUser(serverId, starStatsUserResult))
        );
        return result;
    }

    private CompletableFuture<StarStatsUser> createStarStatsUser(Long serverId, StarStatsUserResult starStatsUserResult) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(starStatsUserResult.getUserId());
        return botService.getMemberInServerAsync(serverId, aUserInAServer.getUserReference().getId()).thenApply(member ->
            StarStatsUser
                    .builder()
                    .starCount(starStatsUserResult.getStarCount())
                    .member(member)
                    .user(AUser.builder().id(starStatsUserResult.getUserId()).build())
                    .build()
        );
    }
}
