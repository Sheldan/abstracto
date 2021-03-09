package dev.sheldan.abstracto.utility.repository.converter;

import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsUser;
import dev.sheldan.abstracto.utility.repository.StarStatsGuildUserResult;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarStatsUserConverterTest {

    @InjectMocks
    private StarStatsUserConverter testUnit;

    @Mock
    private MemberService memberService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private StarStatsUserConverter self;

    @Test
    public void testConversionOfMultipleItems() {
        Long serverId = 5L;
        Long firstUserId = 5L;
        Long secondUserId = 9L;
        List<StarStatsGuildUserResult> results = new ArrayList<>();
        StarStatsGuildUserResult firstResult = Mockito.mock(StarStatsGuildUserResult.class);
        Member firstMember = Mockito.mock(Member.class);
        AUserInAServer firstUser = Mockito.mock(AUserInAServer.class);
        AUser firstAUser = Mockito.mock(AUser.class);
        when(firstAUser.getId()).thenReturn(firstUserId);
        when(firstUser.getUserReference()).thenReturn(firstAUser);
        when(userInServerManagementService.loadOrCreateUser(firstUserId)).thenReturn(firstUser);
        when(memberService.getMemberInServerAsync(serverId, firstUserId)).thenReturn(CompletableFuture.completedFuture(firstMember));
        when(firstResult.getUserId()).thenReturn(firstUserId);
        results.add(firstResult);
        StarStatsGuildUserResult secondResult = Mockito.mock(StarStatsGuildUserResult.class);
        Member secondMember = Mockito.mock(Member.class);
        AUserInAServer secondUser = Mockito.mock(AUserInAServer.class);
        AUser secondAUser = Mockito.mock(AUser.class);
        when(secondAUser.getId()).thenReturn(secondUserId);
        when(secondUser.getUserReference()).thenReturn(secondAUser);
        when(userInServerManagementService.loadOrCreateUser(secondUserId)).thenReturn(secondUser);
        when(memberService.getMemberInServerAsync(serverId, secondUserId)).thenReturn(CompletableFuture.completedFuture(secondMember));

        when(secondResult.getUserId()).thenReturn(secondUserId);
        results.add(secondResult);

        testUnit.convertToStarStatsUser(results, serverId);
        ArgumentCaptor<StarStatsGuildUserResult> resultArgumentCaptor = ArgumentCaptor.forClass(StarStatsGuildUserResult.class);
        ArgumentCaptor<Member> memberArgumentCaptor = ArgumentCaptor.forClass(Member.class);
        verify(self, times(2)).loadStarStatsUser(resultArgumentCaptor.capture(), memberArgumentCaptor.capture());
        List<StarStatsGuildUserResult> resultCaptorValues = resultArgumentCaptor.getAllValues();
        Assert.assertEquals(2, resultCaptorValues.size());
        Assert.assertEquals(firstResult, resultCaptorValues.get(0));
        Assert.assertEquals(secondResult, resultCaptorValues.get(1));
        List<Member> memberCaptorValues = memberArgumentCaptor.getAllValues();
        Assert.assertEquals(2, memberCaptorValues.size());
        Assert.assertEquals(firstMember, memberCaptorValues.get(0));
        Assert.assertEquals(secondMember, memberCaptorValues.get(1));
    }

    @Test
    public void testConversionOfEmptyList() {
        Long serverId = 5L;
        List<StarStatsGuildUserResult> results = new ArrayList<>();

        List<CompletableFuture<StarStatsUser>> starStatsUsers = testUnit.convertToStarStatsUser(results, serverId);
        verify(memberService, times(0)).getMemberInServer(eq(serverId), anyLong());
        Assert.assertEquals(0, starStatsUsers.size());

    }

}
