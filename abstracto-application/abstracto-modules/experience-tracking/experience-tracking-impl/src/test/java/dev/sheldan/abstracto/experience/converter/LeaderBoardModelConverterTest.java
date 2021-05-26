package dev.sheldan.abstracto.experience.converter;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.experience.model.LeaderBoard;
import dev.sheldan.abstracto.experience.model.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.template.LeaderBoardEntryModel;
import net.dv8tion.jda.api.entities.Member;
import org.hibernate.event.spi.ClearEventListener;
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
    private static final Long EXPERIENCE = 9L;
    private static final Long MESSAGES = 10L;
    private static final Integer LEVEL = 54;

    @Test
    public void testFromLeaderBoard() {
        Integer firstRank = 1;

        LeaderBoardEntry entry = getEntry(firstRank, USER_ID, USER_IN_SERVER_ID);
        Integer secondRank = 2;
        LeaderBoardEntry entry2 = getEntry(secondRank, USER_ID_2, USER_IN_SERVER_ID_2);
        List<LeaderBoardEntry> entries = Arrays.asList(entry, entry2);
        LeaderBoard leaderBoard = Mockito.mock(LeaderBoard.class);
        when(leaderBoard.getEntries()).thenReturn(entries);
        Member member = Mockito.mock(Member.class);
        when(member.getIdLong()).thenReturn(USER_ID);
        when(memberService.getMembersInServerAsync(SERVER_ID, Arrays.asList(USER_ID, USER_ID_2))).thenReturn(CompletableFuture.completedFuture(Arrays.asList(member)));
        CompletableFuture<List<LeaderBoardEntryModel>> leaderBoardEntryModels = testUnit.fromLeaderBoard(leaderBoard);
        LeaderBoardEntryModel firstEntry = leaderBoardEntryModels.join().get(0);
        Assert.assertEquals(USER_ID, firstEntry.getUserId());
        LeaderBoardEntryModel secondEntry = leaderBoardEntryModels.join().get(1);
        Assert.assertEquals(USER_ID_2, secondEntry.getUserId());
        Assert.assertEquals(entries.size(), leaderBoardEntryModels.join().size());
    }

    private LeaderBoardEntry getEntry(Integer rank, Long userId, Long userInServerId) {
        AUserExperience experience = Mockito.mock(AUserExperience.class);
        when(experience.getMessageCount()).thenReturn(MESSAGES);
        when(experience.getExperience()).thenReturn(EXPERIENCE);
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        when(experience.getUser()).thenReturn(userInAServer);
        AUser user = Mockito.mock(AUser.class);
        when(userInAServer.getUserReference()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        AServer server = Mockito.mock(AServer.class);
        when(experience.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        LeaderBoardEntry entry =  Mockito.mock(LeaderBoardEntry.class);
        when(entry.getRank()).thenReturn(rank);
        when(entry.getExperience()).thenReturn(experience);
        return entry;
    }


}
