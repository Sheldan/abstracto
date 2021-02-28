package dev.sheldan.abstracto.experience.converter;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.experience.models.LeaderBoard;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
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

    private static final Long SERVER_ID = 4L;
    private static final Long USER_ID = 5L;
    private static final Long USER_ID_2 = 6L;

    @Test
    public void testFromLeaderBoard() {
        Integer firstRank = 1;
        Long firstExperience = 1L;

        LeaderBoardEntry entry = getEntry(firstExperience, firstRank, USER_ID);
        Integer secondRank = 2;
        Long secondExperience = 2L;
        LeaderBoardEntry entry2 = getEntry(secondExperience, secondRank, USER_ID_2);
        List<LeaderBoardEntry> entries = Arrays.asList(entry, entry2);
        LeaderBoard leaderBoard = Mockito.mock(LeaderBoard.class);
        when(leaderBoard.getEntries()).thenReturn(entries);
        Member member = Mockito.mock(Member.class);
        when(memberService.getMemberInServerAsync(SERVER_ID, USER_ID)).thenReturn(CompletableFuture.completedFuture(member));
        when(memberService.getMemberInServerAsync(SERVER_ID, USER_ID_2)).thenReturn(CompletableFuture.completedFuture(member));
        List<CompletableFuture<LeaderBoardEntryModel>> leaderBoardEntryModels = testUnit.fromLeaderBoard(leaderBoard);
        LeaderBoardEntryModel firstEntry = leaderBoardEntryModels.get(0).join();
        Assert.assertEquals(firstRank, firstEntry.getRank());
        Assert.assertEquals(firstExperience, firstEntry.getExperience().getExperience());
        LeaderBoardEntryModel secondEntry = leaderBoardEntryModels.get(1).join();
        Assert.assertEquals(secondRank, secondEntry.getRank());
        Assert.assertEquals(secondExperience, secondEntry.getExperience().getExperience());
        Assert.assertEquals(entries.size(), leaderBoardEntryModels.size());
    }

    @Test
    public void testFromEntry() {
        Integer rank = 2;
        LeaderBoardEntry entry = getEntry(1L, rank, USER_ID);
        Member member = Mockito.mock(Member.class);
        User user = Mockito.mock(User.class);
        when(user.getIdLong()).thenReturn(USER_ID);
        when(member.getUser()).thenReturn(user);
        when(memberService.getMemberInServerAsync(SERVER_ID, USER_ID)).thenReturn(CompletableFuture.completedFuture(member));
        CompletableFuture<LeaderBoardEntryModel> leaderBoardEntryModel = testUnit.fromLeaderBoardEntry(entry);
        LeaderBoardEntryModel entryModel = leaderBoardEntryModel.join();
        Assert.assertEquals(rank, entryModel.getRank());
        Assert.assertEquals(USER_ID, entryModel.getExperience().getUser().getUserReference().getId());
        Assert.assertEquals(USER_ID.longValue(), entryModel.getMember().getUser().getIdLong());
    }

    private LeaderBoardEntry getEntry(Long experienceCount, Integer rank, Long userId) {
        AUserExperience experience = Mockito.mock(AUserExperience.class);
        when(experience.getExperience()).thenReturn(experienceCount);
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        when(experience.getUser()).thenReturn(userInAServer);
        AUser user = Mockito.mock(AUser.class);
        when(userInAServer.getUserReference()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        AServer server = Mockito.mock(AServer.class);
        when(server.getId()).thenReturn(SERVER_ID);
        when(userInAServer.getServerReference()).thenReturn(server);
        LeaderBoardEntry entry =  Mockito.mock(LeaderBoardEntry.class);
        when(entry.getRank()).thenReturn(rank);
        when(entry.getExperience()).thenReturn(experience);
        return entry;
    }


}
