package dev.sheldan.abstracto.experience.converter;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.test.MockUtils;
import dev.sheldan.abstracto.experience.models.LeaderBoard;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.models.templates.LeaderBoardEntryModel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LeaderBoardModelConverterTest {

    @InjectMocks
    public LeaderBoardModelConverter testUnit;

    @Mock
    private MemberService memberService;

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
        Member member = Mockito.mock(Member.class);
        when(memberService.getMemberInServerAsync(server.getId(), entry.getExperience().getUser().getUserReference().getId())).thenReturn(CompletableFuture.completedFuture(member));
        when(memberService.getMemberInServerAsync(server.getId(), entry2.getExperience().getUser().getUserReference().getId())).thenReturn(CompletableFuture.completedFuture(member));
        List<CompletableFuture<LeaderBoardEntryModel>> leaderBoardEntryModels = testUnit.fromLeaderBoard(leaderBoard);
        LeaderBoardEntryModel firstEntry = leaderBoardEntryModels.get(0).join();
        Assert.assertEquals(firstRank, firstEntry.getRank().intValue());
        Assert.assertEquals(firstExperience, firstEntry.getExperience().getExperience().longValue());
        LeaderBoardEntryModel secondEntry = leaderBoardEntryModels.get(1).join();
        Assert.assertEquals(secondRank, secondEntry.getRank().intValue());
        Assert.assertEquals(secondExperience, secondEntry.getExperience().getExperience().longValue());
        Assert.assertEquals(entries.size(), leaderBoardEntryModels.size());
    }

    @Test
    public void testFromEntry() {
        AServer server = MockUtils.getServer();
        Long userId = 3L;
        AUserExperience experience = getUserExperienceObject(server, userId);
        LeaderBoardEntry entry = LeaderBoardEntry.builder().experience(experience).rank(1).build();
        Member member = Mockito.mock(Member.class);
        User user = Mockito.mock(User.class);
        when(user.getIdLong()).thenReturn(userId);
        when(member.getUser()).thenReturn(user);
        when(memberService.getMemberInServerAsync(server.getId(), experience.getUser().getUserReference().getId())).thenReturn(CompletableFuture.completedFuture(member));
        CompletableFuture<LeaderBoardEntryModel> leaderBoardEntryModel = testUnit.fromLeaderBoardEntry(entry);
        LeaderBoardEntryModel entryModel = leaderBoardEntryModel.join();
        Assert.assertEquals(1, entryModel.getRank().intValue());
        Assert.assertEquals(experience.getUser().getUserReference().getId(), entryModel.getExperience().getUser().getUserReference().getId());
        Assert.assertEquals(experience.getUser().getUserReference().getId().longValue(), entryModel.getMember().getUser().getIdLong());
    }

    private LeaderBoardEntry getEntry(AServer server, Integer experienceParameter, Integer rank) {
        AUserExperience firstExperience = getUserExperienceObject(server, experienceParameter);
        return LeaderBoardEntry.builder().rank(rank).experience(firstExperience).build();
    }

    private AUserExperience getUserExperienceObject(AServer server, long i) {
        AUserInAServer userObject = MockUtils.getUserObject(i, server);
        AExperienceLevel level = AExperienceLevel
                .builder()
                .level((int)i)
                .experienceNeeded(i * 100)
                .build();
        return AUserExperience
                .builder()
                .user(userObject)
                .experience(i)
                .currentLevel(level)
                .build();
    }

}
