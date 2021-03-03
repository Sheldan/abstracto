package dev.sheldan.abstracto.experience.converter;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.experience.models.LeaderBoard;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.templates.LeaderBoardEntryModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Converter used to convert from {@link LeaderBoard leaderBoard} to a list of {@link LeaderBoardEntryModel leaderBoardEntryModels}
 */
@Component
@Slf4j
public class LeaderBoardModelConverter {

    @Autowired
    private MemberService memberService;

    /**
     * Converts the complete {@link LeaderBoard leaderBoard} into a list of {@link LeaderBoardEntryModel leaderbaordEntryModels} which contain additional
     * information available for rendering the leader board ({@link Member member} reference and more)
     * @param leaderBoard The {@link LeaderBoard leaderBoard} object to be converted
     * @return The list of {@link LeaderBoardEntryModel leaderboarEntryModels} which contain the fully fledged information provided to the
     * leader board template
     */
    public List<CompletableFuture<LeaderBoardEntryModel>> fromLeaderBoard(LeaderBoard leaderBoard) {
        List<CompletableFuture<LeaderBoardEntryModel>> models = new ArrayList<>();
        log.trace("Converting {} entries to a list of leaderboard entries.", leaderBoard.getEntries().size());
        leaderBoard.getEntries().forEach(leaderBoardEntry -> {
            CompletableFuture<LeaderBoardEntryModel> entry = fromLeaderBoardEntry(leaderBoardEntry);
            models.add(entry);
        });
        return models;
    }

    /**
     * Converts the given {@link LeaderBoardEntry entry} to a {@link LeaderBoardEntryModel model}, which provides a reference to the
     * {@link Member member} object of the given {@link AUserInAServer user} for convenience in the template
     * @param leaderBoardEntry The {@link LeaderBoardEntry entry} to be converted
     * @return The {@link LeaderBoardEntryModel model} accompanied with the {@link Member member} reference, might be null, if the
     * user left the guild
     */
    public CompletableFuture<LeaderBoardEntryModel> fromLeaderBoardEntry(LeaderBoardEntry leaderBoardEntry) {
        AUserInAServer entryUser = leaderBoardEntry.getExperience().getUser();
        return memberService.getMemberInServerAsync(entryUser.getServerReference().getId(), entryUser.getUserReference().getId()).thenApply(member ->
            LeaderBoardEntryModel
                    .builder()
                    .experience(leaderBoardEntry.getExperience())
                    .member(member).rank(leaderBoardEntry.getRank())
                    .rank(leaderBoardEntry.getRank())
                    .build()
        );
    }
}
