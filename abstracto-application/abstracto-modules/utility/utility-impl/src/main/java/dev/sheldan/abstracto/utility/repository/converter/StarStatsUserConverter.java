package dev.sheldan.abstracto.utility.repository.converter;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsUser;
import dev.sheldan.abstracto.utility.repository.StarStatsGuildUserResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class StarStatsUserConverter {

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private StarStatsUserConverter self;

    public List<CompletableFuture<StarStatsUser>> convertToStarStatsUser(List<StarStatsGuildUserResult> users, Long serverId) {
        List<CompletableFuture<StarStatsUser>> result = new ArrayList<>();
        users.forEach(starStatsUserResult ->
            result.add(createStarStatsUser(serverId, starStatsUserResult))
        );
        return result;
    }

    private CompletableFuture<StarStatsUser> createStarStatsUser(Long serverId, StarStatsGuildUserResult starStatsGuildUserResult) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(starStatsGuildUserResult.getUserId());
        return memberService.getMemberInServerAsync(serverId, aUserInAServer.getUserReference().getId())
                .thenApply(member -> self.loadStarStatsUser(starStatsGuildUserResult, member))
                .exceptionally(throwable -> self.loadStarStatsUser(starStatsGuildUserResult, null));
    }

    @Transactional
    public StarStatsUser loadStarStatsUser(StarStatsGuildUserResult starStatsGuildUserResult, net.dv8tion.jda.api.entities.Member member) {
        return StarStatsUser
                .builder()
                .starCount(starStatsGuildUserResult.getStarCount())
                .member(member)
                .user(userInServerManagementService.loadOrCreateUser(starStatsGuildUserResult.getUserId()))
                .build();
    }
}
