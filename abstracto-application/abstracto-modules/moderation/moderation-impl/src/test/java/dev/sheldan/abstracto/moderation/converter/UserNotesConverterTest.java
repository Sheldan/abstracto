package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.models.database.UserNote;
import dev.sheldan.abstracto.moderation.models.template.commands.NoteEntryModel;
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
public class UserNotesConverterTest {

    @InjectMocks
    private UserNotesConverter testUnit;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private BotService botService;

    @Test
    public void testWithEmptyList() {
        List<NoteEntryModel> entryModels = testUnit.fromNotes(Collections.emptyList());
        Assert.assertEquals(0, entryModels.size());
    }

    @Test
    public void testWithSomeUserNotes() {
        AServer server = MockUtils.getServer();
        AUserInAServer userInAServer = MockUtils.getUserObject(4L, server);
        Member member = Mockito.mock(Member.class);
        when(botService.getMemberInServer(userInAServer)).thenReturn(member);
        UserNote firstNote = UserNote.builder().user(userInAServer).build();
        UserNote secondNote = UserNote.builder().user(userInAServer).build();
        List<NoteEntryModel> entryModels = testUnit.fromNotes(Arrays.asList(firstNote, secondNote));
        Assert.assertEquals(2, entryModels.size());
        NoteEntryModel firstEntry = entryModels.get(0);
        Assert.assertEquals(member, firstEntry.getFullUser().getMember());
        Assert.assertEquals(userInAServer, firstEntry.getFullUser().getAUserInAServer());
        NoteEntryModel secondEntry = entryModels.get(1);
        Assert.assertEquals(member, secondEntry.getFullUser().getMember());
        Assert.assertEquals(userInAServer, secondEntry.getFullUser().getAUserInAServer());
    }

}
