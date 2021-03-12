package dev.sheldan.abstracto.repostdetection.converter;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.repostdetection.model.RepostLeaderboardEntryModel;
import dev.sheldan.abstracto.repostdetection.model.database.result.RepostLeaderboardResult;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepostLeaderBoardConverterTest {

    @InjectMocks
    private RepostLeaderBoardConverter testUnit;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private MemberService memberService;

    @Mock
    private RepostLeaderBoardConverter self;

    @Mock
    private AUserInAServer aUserInAServer;

    @Mock
    private Member member;

    private static final Long USER_IN_SERVER_ID = 1L;
    private static final Integer REPOST_COUNT = 1;
    private static final Integer RANK = 2;

    @Test
    public void testConvertEmptyList() {
        CompletableFuture<List<RepostLeaderboardEntryModel>> future = testUnit.fromLeaderBoardResults(new ArrayList<>());
        Assert.assertTrue(future.isDone());
        Assert.assertEquals(0, future.join().size());
    }

    @Test
    public void testConvertOneResult() {
        RepostLeaderboardResult result = setupFirstResult();
        CompletableFuture<List<RepostLeaderboardEntryModel>> future = testUnit.fromLeaderBoardResults(Arrays.asList(result));
        Assert.assertTrue(future.isDone());
        Assert.assertEquals(1, future.join().size());
        verifyFirstResultConversion();
    }

    @Test
    public void testConvertTwoResults() {
        RepostLeaderboardResult result = setupFirstResult();
        RepostLeaderboardResult result2 = Mockito.mock(RepostLeaderboardResult.class);
        Long secondUserInServerId = USER_IN_SERVER_ID + 1;
        when(result2.getUserInServerId()).thenReturn(secondUserInServerId);
        Integer secondRepostCount = REPOST_COUNT + 1;
        when(result2.getRepostCount()).thenReturn(secondRepostCount);
        Integer secondRank = RANK + 1;
        when(result2.getRank()).thenReturn(secondRank);
        AUserInAServer secondAUserInAServer = Mockito.mock(AUserInAServer.class);
        when(userInServerManagementService.loadOrCreateUser(secondUserInServerId)).thenReturn(secondAUserInAServer);
        Member secondMember = Mockito.mock(Member.class);
        when(memberService.getMemberInServerAsync(secondAUserInAServer)).thenReturn(CompletableFuture.completedFuture(secondMember));
        CompletableFuture<List<RepostLeaderboardEntryModel>> future = testUnit.fromLeaderBoardResults(Arrays.asList(result, result2));
        Assert.assertTrue(future.isDone());
        Assert.assertEquals(2, future.join().size());
        verifyFirstResultConversion();
        verify(self, times(1)).loadUserFromDatabase(secondMember, secondRepostCount, secondUserInServerId, secondRank);
    }

    private void verifyFirstResultConversion() {
        verify(self, times(1)).loadUserFromDatabase(member, REPOST_COUNT, USER_IN_SERVER_ID, RANK);
    }

    private RepostLeaderboardResult setupFirstResult() {
        RepostLeaderboardResult result = Mockito.mock(RepostLeaderboardResult.class);
        when(result.getUserInServerId()).thenReturn(USER_IN_SERVER_ID);
        when(result.getRepostCount()).thenReturn(REPOST_COUNT);
        when(result.getRank()).thenReturn(RANK);
        when(userInServerManagementService.loadOrCreateUser(USER_IN_SERVER_ID)).thenReturn(aUserInAServer);
        when(memberService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(member));
        return result;
    }

    @Test
    public void testLoadUserFromDatabase() {
        when(userInServerManagementService.loadOrCreateUser(USER_IN_SERVER_ID)).thenReturn(aUserInAServer);
        RepostLeaderboardEntryModel loadedModel = testUnit.loadUserFromDatabase(member, REPOST_COUNT, USER_IN_SERVER_ID, RANK);
        Assert.assertEquals(REPOST_COUNT, loadedModel.getCount());
        Assert.assertEquals(RANK, loadedModel.getRank());
        Assert.assertEquals(member, loadedModel.getMember());
        Assert.assertEquals(aUserInAServer, loadedModel.getUser());
    }

}
