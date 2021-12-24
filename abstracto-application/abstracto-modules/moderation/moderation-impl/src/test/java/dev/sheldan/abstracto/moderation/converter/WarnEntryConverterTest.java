package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.models.FutureMemberPair;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.model.database.Warning;
import dev.sheldan.abstracto.moderation.model.template.command.WarnEntry;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class WarnEntryConverterTest {

    @InjectMocks
    private WarnEntryConverter testUnit;

    @Mock
    private MemberService memberService;

    @Mock
    private WarnEntryConverter self;

    @Mock
    private WarnManagementService warnManagementService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    private static final Long SERVER_ID = 5L;
    private static final Long WARN_ID_1 = 6L;
    private static final Long WARN_ID_2 = 7L;
    private static final Long USER_ID_1 = 8L;
    private static final Long USER_ID_2 = 9L;

    @Test
    public void testWithEmptyList() {
        CompletableFuture<List<WarnEntry>> entryModels = testUnit.fromWarnings(Collections.emptyList());
        Assert.assertEquals(0, entryModels.join().size());
    }

    @Test
    public void testWithSomeWarnings() {
        AUserInAServer warnedUser = Mockito.mock(AUserInAServer.class);
        AUserInAServer warningUser = Mockito.mock(AUserInAServer.class);
        Member warnedMember = Mockito.mock(Member.class);
        Member warningMember = Mockito.mock(Member.class);
        when(memberService.getMemberInServerAsync(warnedUser)).thenReturn(CompletableFuture.completedFuture(warnedMember));
        when(memberService.getMemberInServerAsync(warningUser)).thenReturn(CompletableFuture.completedFuture(warningMember));
        Warning firstWarning = Mockito.mock(Warning.class);
        when(firstWarning.getWarningUser()).thenReturn(warningUser);
        when(firstWarning.getWarnedUser()).thenReturn(warnedUser);
        Warning secondWarning = Mockito.mock(Warning.class);
        when(secondWarning.getWarningUser()).thenReturn(warningUser);
        when(secondWarning.getWarnedUser()).thenReturn(warnedUser);
        List<WarnEntry> loaded = new ArrayList<>();
        when(self.loadFullWarnEntries(any())).thenReturn(loaded);
        CompletableFuture<List<WarnEntry>> future = testUnit.fromWarnings(Arrays.asList(firstWarning, secondWarning));
        List<WarnEntry> entries = future.join();
        Assert.assertFalse(future.isCompletedExceptionally());
        Assert.assertEquals(loaded, entries);
        verify(self, times(1)).loadFullWarnEntries(any());
    }

    @Test
    public void testLoadingFullWarnings() {
        AUserInAServer warnedUser = Mockito.mock(AUserInAServer.class);
        AUserInAServer warningUser = Mockito.mock(AUserInAServer.class);
        AUser firstUser = Mockito.mock(AUser.class);
        when(firstUser.getId()).thenReturn(USER_ID_1);
        when(warnedUser.getUserReference()).thenReturn(firstUser);
        AUser secondUser = Mockito.mock(AUser.class);
        when(secondUser.getId()).thenReturn(USER_ID_2);
        when(warningUser.getUserReference()).thenReturn(secondUser);
        Member warningMember = Mockito.mock(Member.class);
        Member warnedMember = Mockito.mock(Member.class);
        Warning warning1 = Mockito.mock(Warning.class);
        Warning warning2 = Mockito.mock(Warning.class);
        ServerSpecificId firstWarnId = new ServerSpecificId(SERVER_ID, WARN_ID_1);
        when(warning1.getWarnId()).thenReturn(firstWarnId);
        when(warning1.getWarningUser()).thenReturn(warningUser);
        when(warning1.getWarnedUser()).thenReturn(warnedUser);
        ServerSpecificId secondWarnId = new ServerSpecificId(SERVER_ID, WARN_ID_2);
        when(warning2.getWarnId()).thenReturn(secondWarnId);
        when(warning2.getWarningUser()).thenReturn(warningUser);
        when(warning2.getWarnedUser()).thenReturn(warnedUser);
        HashMap<ServerSpecificId, FutureMemberPair> map = new HashMap<>();
        FutureMemberPair memberPair = Mockito.mock(FutureMemberPair.class);
        when(memberPair.getFirstMember()).thenReturn(CompletableFuture.completedFuture(warningMember));
        when(memberPair.getSecondMember()).thenReturn(CompletableFuture.completedFuture(warnedMember));
        map.put(firstWarnId, memberPair);
        map.put(secondWarnId, memberPair);
        when(warnManagementService.findById(WARN_ID_1, SERVER_ID)).thenReturn(warning1);
        when(warnManagementService.findById(WARN_ID_2, SERVER_ID)).thenReturn(warning2);
        List<WarnEntry> models = testUnit.loadFullWarnEntries(map);
        Assert.assertEquals(2, models.size());
        WarnEntry firstEntry = models.get(0);
        Assert.assertEquals(USER_ID_1, firstEntry.getWarnedUser().getUserId());
        Assert.assertEquals(USER_ID_2, firstEntry.getWarningUser().getUserId());
        Assert.assertEquals(WARN_ID_1, firstEntry.getWarnId());
        Assert.assertEquals(SERVER_ID, firstEntry.getServerId());
        WarnEntry secondEntry = models.get(1);
        Assert.assertEquals(USER_ID_1, secondEntry.getWarnedUser().getUserId());
        Assert.assertEquals(USER_ID_2, secondEntry.getWarningUser().getUserId());
        Assert.assertEquals(WARN_ID_2, secondEntry.getWarnId());
        Assert.assertEquals(SERVER_ID, secondEntry.getServerId());
    }
}
