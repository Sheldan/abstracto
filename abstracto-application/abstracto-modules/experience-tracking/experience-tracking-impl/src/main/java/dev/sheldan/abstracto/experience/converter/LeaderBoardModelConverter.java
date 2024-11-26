package dev.sheldan.abstracto.experience.converter;

import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.experience.model.LeaderBoard;
import dev.sheldan.abstracto.experience.model.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.model.template.LeaderBoardEntryModel;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converter used to convert from {@link LeaderBoard leaderBoard} to a list of {@link LeaderBoardEntryModel leaderBoardEntryModels}
 */
@Component
@Slf4j
public class LeaderBoardModelConverter {

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    @Autowired
    private LeaderBoardModelConverter self;

    /**
     * Converts the complete {@link LeaderBoard leaderBoard} into a list of {@link LeaderBoardEntryModel leaderbaordEntryModels} which contain additional
     * information available for rendering the leader board ({@link Member member} reference and more)
     * @param leaderBoard The {@link LeaderBoard leaderBoard} object to be converted
     * @return The list of {@link LeaderBoardEntryModel leaderboarEntryModels} which contain the fully fledged information provided to the
     * leader board template
     */
    public CompletableFuture<List<LeaderBoardEntryModel>> fromLeaderBoard(LeaderBoard leaderBoard, Long serverId) {
        log.debug("Converting {} entries to a list of leaderboard entries.", leaderBoard.getEntries().size());
        return fromLeaderBoardEntry(leaderBoard.getEntries(), serverId);
    }

    public CompletableFuture<List<LeaderBoardEntryModel>> fromLeaderBoardEntry(List<LeaderBoardEntry> leaderBoardEntries, Long serverId) {
        List<Long> userIds = new ArrayList<>();
        Map<Long, LeaderBoardEntryModel> models = leaderBoardEntries
                .stream()
                .map(leaderBoardEntry -> {
                    userIds.add(leaderBoardEntry.getUserId());
                    return LeaderBoardEntryModel
                            .builder()
                            .userId(leaderBoardEntry.getUserId())
                            .experience(leaderBoardEntry.getExperience())
                            .messageCount(leaderBoardEntry.getMessageCount())
                            .level(leaderBoardEntry.getLevel())
                            .rank(leaderBoardEntry.getRank())
                            .build();
                })
                .collect(Collectors.toMap(LeaderBoardEntryModel::getUserId, Function.identity()));
        return memberService.getMembersInServerAsync(serverId, userIds).thenApply(members -> {
            members.forEach(member -> models.get(member.getIdLong()).setMember(member));
            return new ArrayList<>(models.values())
                    .stream()
                    .sorted(Comparator.comparing(LeaderBoardEntryModel::getRank)).
                            collect(Collectors.toList());
        });
    }
}
