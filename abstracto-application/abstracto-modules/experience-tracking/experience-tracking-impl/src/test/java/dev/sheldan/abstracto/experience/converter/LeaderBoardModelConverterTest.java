package dev.sheldan.abstracto.experience.converter;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.experience.model.LeaderBoard;
import dev.sheldan.abstracto.experience.model.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.template.LeaderBoardEntryModel;
import net.dv8tion.jda.api.entities.Member;
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

    @Mock
    private LeaderBoardModelConverter self;

    private static final Long SERVER_ID = 4L;
    private static final Long USER_ID = 5L;
    private static final Long USER_ID_2 = 6L;
    private static final Long USER_IN_SERVER_ID = 7L;
    private static final Long USER_IN_SERVER_ID_2 = 8L;

    @Test
    public void testFromLeaderBoard() {
        Integer firstRank = 1;

        LeaderBoardEntry entry = getEntry(firstRank, USER_ID, USER_IN_SERVER_ID);
        Integer secondRank = 2;
        LeaderBoardEntryModel firstEntryModel = Mockito.mock(LeaderBoardEntryModel.class);
        LeaderBoardEntryModel secondEntryModel = Mockito.mock(LeaderBoardEntryModel.class);
        LeaderBoardEntry entry2 = getEntry(secondRank, USER_ID_2, USER_IN_SERVER_ID_2);
        List<LeaderBoardEntry> entries = Arrays.asList(entry, entry2);
        LeaderBoard leaderBoard = Mockito.mock(LeaderBoard.class);
        when(leaderBoard.getEntries()).thenReturn(entries);
        Member member = Mockito.mock(Member.class);
        when(memberService.getMemberInServerAsync(SERVER_ID, USER_ID)).thenReturn(CompletableFuture.completedFuture(member));
        when(memberService.getMemberInServerAsync(SERVER_ID, USER_ID_2)).thenReturn(CompletableFuture.completedFuture(member));
        when(self.buildLeaderBoardModel(USER_IN_SERVER_ID, member, firstRank)).thenReturn(firstEntryModel);
        when(self.buildLeaderBoardModel(USER_IN_SERVER_ID_2, member, secondRank)).thenReturn(secondEntryModel);
        List<CompletableFuture<LeaderBoardEntryModel>> leaderBoardEntryModels = testUnit.fromLeaderBoard(leaderBoard);
        LeaderBoardEntryModel firstEntry = leaderBoardEntryModels.get(0).join();
        Assert.assertEquals(firstEntryModel, firstEntry);
        LeaderBoardEntryModel secondEntry = leaderBoardEntryModels.get(1).join();
        Assert.assertEquals(secondEntryModel, secondEntry);
        Assert.assertEquals(entries.size(), leaderBoardEntryModels.size());
    }

    @Test
    public void testFromEntry() {
        Integer rank = 2;
        LeaderBoardEntry entry = getEntry(rank, USER_ID, USER_IN_SERVER_ID);
        Member member = Mockito.mock(Member.class);
        LeaderBoardEntryModel entryModelMock = Mockito.mock(LeaderBoardEntryModel.class);
        when(memberService.getMemberInServerAsync(SERVER_ID, USER_ID)).thenReturn(CompletableFuture.completedFuture(member));
        when(self.buildLeaderBoardModel(USER_IN_SERVER_ID, member, rank)).thenReturn(entryModelMock);
        CompletableFuture<LeaderBoardEntryModel> leaderBoardEntryModel = testUnit.fromLeaderBoardEntry(entry);
        LeaderBoardEntryModel entryModel = leaderBoardEntryModel.join();
        Assert.assertEquals(entryModelMock, entryModel);
    }

    private LeaderBoardEntry getEntry(Integer rank, Long userId, Long userInServerId) {
        AUserExperience experience = Mockito.mock(AUserExperience.class);
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        when(experience.getUser()).thenReturn(userInAServer);
        AUser user = Mockito.mock(AUser.class);
        when(userInAServer.getUserReference()).thenReturn(user);
        when(userInAServer.getUserInServerId()).thenReturn(userInServerId);
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
