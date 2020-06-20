package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnEntry;
import dev.sheldan.abstracto.test.MockUtils;
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
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WarnEntryConverterTest {

    @InjectMocks
    private WarnEntryConverter testUnit;

    @Mock
    private BotService botService;

    @Test
    public void testWithEmptyList() {
        List<WarnEntry> entryModels = testUnit.fromWarnings(Collections.emptyList());
        Assert.assertEquals(0, entryModels.size());
    }

    @Test
    public void testWithSomeUserNotes() {
        AServer server = MockUtils.getServer();
        AUserInAServer warnedUserInAServer = MockUtils.getUserObject(4L, server);
        AUserInAServer warningUserInAServer = MockUtils.getUserObject(6L, server);
        Member warnedMember = Mockito.mock(Member.class);
        Member warningMember = Mockito.mock(Member.class);
        when(botService.getMemberInServer(warnedUserInAServer)).thenReturn(warnedMember);
        when(botService.getMemberInServer(warningUserInAServer)).thenReturn(warningMember);
        Warning firstNote = Warning.builder().warnedUser(warnedUserInAServer).warningUser(warningUserInAServer).build();
        Warning secondNote = Warning.builder().warnedUser(warnedUserInAServer).warningUser(warningUserInAServer).build();
        List<WarnEntry> entryModels = testUnit.fromWarnings(Arrays.asList(firstNote, secondNote));
        Assert.assertEquals(2, entryModels.size());
        WarnEntry firstEntry = entryModels.get(0);
        Assert.assertEquals(warningMember, firstEntry.getWarningUser().getMember());
        Assert.assertEquals(warnedMember, firstEntry.getWarnedUser().getMember());
        Assert.assertEquals(warningUserInAServer, firstEntry.getWarningUser().getAUserInAServer());
        Assert.assertEquals(warnedUserInAServer, firstEntry.getWarnedUser().getAUserInAServer());
        WarnEntry secondEntry = entryModels.get(1);
        Assert.assertEquals(warningMember, secondEntry.getWarningUser().getMember());
        Assert.assertEquals(warnedMember, secondEntry.getWarnedUser().getMember());
        Assert.assertEquals(warningUserInAServer, secondEntry.getWarningUser().getAUserInAServer());
        Assert.assertEquals(warnedUserInAServer, secondEntry.getWarnedUser().getAUserInAServer());
    }
}
