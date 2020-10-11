package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.models.FutureMemberPair;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnEntry;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class WarnEntryConverterTest {

    @InjectMocks
    private WarnEntryConverter testUnit;

    @Mock
    private BotService botService;

    @Mock
    private WarnEntryConverter self;

    @Mock
    private WarnManagementService warnManagementService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    private static final Long SERVER_ID = 5L;
    private static final Long WARN_ID_1 = 6L;
    private static final Long WARN_ID_2 = 7L;

    @Test
    public void testWithEmptyList() {
        CompletableFuture<List<WarnEntry>> entryModels = testUnit.fromWarnings(Collections.emptyList());
        Assert.assertEquals(0, entryModels.join().size());
    }

    @Test
    public void testWithSomeUserNotes() {
        AUserInAServer warnedUser = Mockito.mock(AUserInAServer.class);
        AUserInAServer warningUser = Mockito.mock(AUserInAServer.class);
        Member warnedMember = Mockito.mock(Member.class);
        Member warningMember = Mockito.mock(Member.class);
        when(botService.getMemberInServerAsync(warnedUser)).thenReturn(CompletableFuture.completedFuture(warnedMember));
        when(botService.getMemberInServerAsync(warningUser)).thenReturn(CompletableFuture.completedFuture(warningMember));
        Warning firstNote = Warning.builder().warnId(new ServerSpecificId(3L, 4L)).warnedUser(warnedUser).warningUser(warningUser).build();
        Warning secondNote = Warning.builder().warnId(new ServerSpecificId(3L, 5L)).warnedUser(warnedUser).warningUser(warningUser).build();
        testUnit.fromWarnings(Arrays.asList(firstNote, secondNote));
        verify(self, times(1)).loadFullWarnEntries(any());
    }

    @Test
    public void testLoadingFullNotes() {
        AUserInAServer warnedUser = Mockito.mock(AUserInAServer.class);
        AUserInAServer warningUser = Mockito.mock(AUserInAServer.class);
        Member warningMember = Mockito.mock(Member.class);
        Member warnedMember = Mockito.mock(Member.class);
        Warning warning1 = Mockito.mock(Warning.class);
        Warning warning2 = Mockito.mock(Warning.class);
        ServerSpecificId firstWarnId = new ServerSpecificId(SERVER_ID, WARN_ID_1);
        when(warning1.getWarnId()).thenReturn(firstWarnId);
        ServerSpecificId secondWarnId = new ServerSpecificId(SERVER_ID, WARN_ID_2);
        when(warning2.getWarnId()).thenReturn(secondWarnId);
        HashMap<ServerSpecificId, FutureMemberPair> map = new HashMap<>();
        FutureMemberPair memberPair = FutureMemberPair.builder().firstMember(CompletableFuture.completedFuture(warningMember)).secondMember(CompletableFuture.completedFuture(warnedMember)).build();
        map.put(firstWarnId, memberPair);
        map.put(secondWarnId, memberPair);
        when(warnManagementService.findById(WARN_ID_1, SERVER_ID)).thenReturn(warning1);
        when(warnManagementService.findById(WARN_ID_2, SERVER_ID)).thenReturn(warning2);
        when(userInServerManagementService.loadUser(warnedMember)).thenReturn(warnedUser);
        when(userInServerManagementService.loadUser(warningMember)).thenReturn(warningUser);
        List<WarnEntry> models = testUnit.loadFullWarnEntries(map);
        Assert.assertEquals(2, models.size());
        WarnEntry firstEntry = models.get(0);
        Assert.assertEquals(warningMember, firstEntry.getWarningUser().getMember());
        Assert.assertEquals(warnedMember, firstEntry.getWarnedUser().getMember());
        Assert.assertEquals(warnedUser, firstEntry.getWarnedUser().getAUserInAServer());
        Assert.assertEquals(warningUser, firstEntry.getWarningUser().getAUserInAServer());
        Assert.assertEquals(WARN_ID_1, firstEntry.getWarning().getWarnId().getId());
        Assert.assertEquals(SERVER_ID, firstEntry.getWarning().getWarnId().getServerId());
        WarnEntry secondEntry = models.get(1);
        Assert.assertEquals(warningMember, secondEntry.getWarningUser().getMember());
        Assert.assertEquals(warnedMember, secondEntry.getWarnedUser().getMember());
        Assert.assertEquals(warnedUser, secondEntry.getWarnedUser().getAUserInAServer());
        Assert.assertEquals(warningUser, secondEntry.getWarningUser().getAUserInAServer());
        Assert.assertEquals(WARN_ID_2, secondEntry.getWarning().getWarnId().getId());
        Assert.assertEquals(SERVER_ID, secondEntry.getWarning().getWarnId().getServerId());
    }
}
