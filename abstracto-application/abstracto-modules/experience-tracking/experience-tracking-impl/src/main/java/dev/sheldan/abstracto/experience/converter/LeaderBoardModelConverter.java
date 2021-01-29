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
 * Converter used to convert from {@link LeaderBoard} to a list of {@link LeaderBoardEntryModel}
 */
@Component
@Slf4j
public class LeaderBoardModelConverter {

    @Autowired
    private MemberService memberService;

    /**
     * Converts the complete {@link LeaderBoard} into a list of {@link LeaderBoardEntryModel} which contain additional
     * information available for rendering the leader board ({@link Member} reference and more)
     * @param leaderBoard The {@link LeaderBoard} object to be converted
     * @return The list of {@link LeaderBoardEntryModel} which contain the fully fledged information provided to the
     * leader board template
     */
    public List<CompletableFuture<LeaderBoardEntryModel>> fromLeaderBoard(LeaderBoard leaderBoard) {
        List<CompletableFuture<LeaderBoardEntryModel>> models = new ArrayList<>();
        log.trace("Converting {} entries to a list of leaderbord entries.", leaderBoard.getEntries().size());
        leaderBoard.getEntries().forEach(leaderBoardEntry -> {
            CompletableFuture<LeaderBoardEntryModel> entry = fromLeaderBoardEntry(leaderBoardEntry);
            models.add(entry);
        });
        return models;
    }

    /**
     * Converts the given {@link LeaderBoardEntry} to a {@link LeaderBoardEntryModel}, which provides a reference to the
     * {@link Member} object of the given {@link AUserInAServer} for convenience in the template
     * @param leaderBoardEntry The {@link LeaderBoardEntry} to be converted
     * @return The {@link LeaderBoardEntryModel} accompanied with the {@link Member} reference, might be null, if the
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
