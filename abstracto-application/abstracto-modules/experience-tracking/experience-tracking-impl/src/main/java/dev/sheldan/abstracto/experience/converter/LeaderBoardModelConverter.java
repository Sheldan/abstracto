package dev.sheldan.abstracto.experience.converter;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.experience.models.LeaderBoard;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.templates.LeaderBoardEntryModel;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter used to conver from {@link LeaderBoard} to a list of {@link LeaderBoardEntryModel}
 */
@Component
public class LeaderBoardModelConverter {

    @Autowired
    private BotService botService;

    /**
     * Converts the complete {@link LeaderBoard} into a list of {@link LeaderBoardEntryModel} which contain additional
     * information available for rendering the leaderboard ({@link Member} reference)
     * @param leaderBoard The {@link LeaderBoard} object to be converted
     * @return The list of {@link LeaderBoardEntryModel} which contain the fully fledged information provided to the
     * leaderboard template
     */
    public List<LeaderBoardEntryModel> fromLeaderBoard(LeaderBoard leaderBoard) {
        List<LeaderBoardEntryModel> models = new ArrayList<>();
        leaderBoard.getEntries().forEach(leaderBoardEntry -> {
            LeaderBoardEntryModel entry = fromLeaderBoardEntry(leaderBoardEntry);
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
    public LeaderBoardEntryModel fromLeaderBoardEntry(LeaderBoardEntry leaderBoardEntry) {
        AUserInAServer entryUser = leaderBoardEntry.getExperience().getUser();
        Member entryMember = botService.getMemberInServer(entryUser.getServerReference().getId(), entryUser.getUserReference().getId());
        return LeaderBoardEntryModel
                .builder()
                .experience(leaderBoardEntry.getExperience())
                .member(entryMember).rank(leaderBoardEntry.getRank())
                .rank(leaderBoardEntry.getRank())
                .build();
    }
}
