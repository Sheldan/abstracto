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

@Component
public class LeaderBoardModelConverter {

    @Autowired
    private BotService botService;

    public List<LeaderBoardEntryModel> fromLeaderBoard(LeaderBoard leaderBoard) {
        List<LeaderBoardEntryModel> models = new ArrayList<>();
        leaderBoard.getEntries().forEach(leaderBoardEntry -> {
            LeaderBoardEntryModel entry = fromLeaderBoardEntry(leaderBoardEntry);
            models.add(entry);
        });
        return models;
    }

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
