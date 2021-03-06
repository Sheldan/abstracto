package dev.sheldan.abstracto.repostdetection.converter;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.repostdetection.model.RepostLeaderboardEntryModel;
import dev.sheldan.abstracto.repostdetection.model.database.result.RepostLeaderboardResult;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class RepostLeaderBoardConverter {

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private RepostLeaderBoardConverter self;

    public CompletableFuture<List<RepostLeaderboardEntryModel>> fromLeaderBoardResults(List<RepostLeaderboardResult> results) {
        if(results.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        List<CompletableFuture<RepostLeaderboardEntryModel>> modelFutures =
                results.stream().map(this::convertSingleUser).collect(Collectors.toList());
        return FutureUtils.toSingleFutureGeneric(modelFutures).thenApply(unused ->
            modelFutures.stream().map(CompletableFuture::join).collect(Collectors.toList())
        );
    }

    public CompletableFuture<RepostLeaderboardEntryModel> convertSingleUser(RepostLeaderboardResult result) {
        AUserInAServer user = userInServerManagementService.loadOrCreateUser(result.getUserInServerId());
        Integer count = result.getRepostCount();
        Long userInServerId = result.getUserInServerId();
        Integer rank = result.getRank();
        return memberService.getMemberInServerAsync(user).thenApply(member ->
            self.loadUserFromDatabase(member, count, userInServerId, rank)
        );
    }

    @Transactional
    public RepostLeaderboardEntryModel loadUserFromDatabase(Member member, Integer count, Long userInServerId, Integer rank) {
        return RepostLeaderboardEntryModel
                .builder()
                .member(member)
                .user(userInServerManagementService.loadOrCreateUser(userInServerId))
                .count(count)
                .rank(rank)
                .build();
    }
}
