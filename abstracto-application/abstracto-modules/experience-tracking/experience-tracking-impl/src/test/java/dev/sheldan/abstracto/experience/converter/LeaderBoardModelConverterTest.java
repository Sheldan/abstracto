package dev.sheldan.abstracto.experience.converter;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.experience.ExperienceRelatedTest;
import dev.sheldan.abstracto.experience.models.LeaderBoard;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.models.templates.LeaderBoardEntryModel;
import dev.sheldan.abstracto.test.MockUtils;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LeaderBoardModelConverterTest extends ExperienceRelatedTest {

    @InjectMocks
    public LeaderBoardModelConverter testUnit;

    @Mock
    private BotService botService;

    @Mock
    private JDAImpl jda;

    @Test
    public void testFromLeaderBoard() {
        AServer server = MockUtils.getServer();
        int firstRank = 1;
        int firstExperience = 1;
        LeaderBoardEntry entry = getEntry(server, firstExperience, firstRank);
        int secondRank = 2;
        int secondExperience = 2;
        LeaderBoardEntry entry2 = getEntry(server, secondExperience, secondRank);
        List<LeaderBoardEntry> entries = Arrays.asList(entry, entry2);
        LeaderBoard leaderBoard = LeaderBoard.builder().entries(entries).build();
        List<LeaderBoardEntryModel> leaderBoardEntryModels = testUnit.fromLeaderBoard(leaderBoard);
        LeaderBoardEntryModel firstEntry = leaderBoardEntryModels.get(0);
        Assert.assertEquals(firstRank, firstEntry.getRank().intValue());
        Assert.assertEquals(firstExperience, firstEntry.getExperience().getExperience().longValue());
        LeaderBoardEntryModel secondEntry = leaderBoardEntryModels.get(1);
        Assert.assertEquals(secondRank, secondEntry.getRank().intValue());
        Assert.assertEquals(secondExperience, secondEntry.getExperience().getExperience().longValue());
        Assert.assertEquals(entries.size(), leaderBoardEntryModels.size());
    }

    @Test
    public void testFromEntry() {
        AServer server = MockUtils.getServer();
        AUserExperience experience = getUserExperienceObject(server, 3);
        LeaderBoardEntry entry = LeaderBoardEntry.builder().experience(experience).rank(1).build();
        MemberImpl member = MockUtils.getMockedMember(server, experience.getUser(), jda);
        when(botService.getMemberInServer(server.getId(), experience.getUser().getUserReference().getId())).thenReturn(member);
        LeaderBoardEntryModel leaderBoardEntryModel = testUnit.fromLeaderBoardEntry(entry);
        Assert.assertEquals(1, leaderBoardEntryModel.getRank().intValue());
        Assert.assertEquals(experience.getUser().getUserReference().getId(), leaderBoardEntryModel.getExperience().getUser().getUserReference().getId());
        Assert.assertEquals(experience.getUser().getUserReference().getId().longValue(), leaderBoardEntryModel.getMember().getUser().getIdLong());
    }

    private LeaderBoardEntry getEntry(AServer server, Integer experienceParameter, Integer rank) {
        AUserExperience firstExperience = getUserExperienceObject(server, experienceParameter);
        return LeaderBoardEntry.builder().rank(rank).experience(firstExperience).build();
    }
}
