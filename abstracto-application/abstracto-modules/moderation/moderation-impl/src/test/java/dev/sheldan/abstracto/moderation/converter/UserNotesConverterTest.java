package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.models.database.UserNote;
import dev.sheldan.abstracto.moderation.models.template.commands.NoteEntryModel;
import dev.sheldan.abstracto.moderation.service.management.UserNoteManagementService;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserNotesConverterTest {

    @InjectMocks
    private UserNotesConverter testUnit;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private BotService botService;

    @Mock
    private UserNotesConverter self;

    @Mock
    private UserNoteManagementService userNoteManagementService;

    @Test
    public void testWithEmptyList() {
        CompletableFuture<List<NoteEntryModel>> entryModels = testUnit.fromNotes(Collections.emptyList());
        Assert.assertEquals(0, entryModels.join().size());
    }

    @Test
    public void testWithSomeUserNotes() {
        AServer server = MockUtils.getServer();
        AUserInAServer userInAServer = MockUtils.getUserObject(4L, server);
        Member member = Mockito.mock(Member.class);
        when(botService.getMemberInServerAsync(userInAServer)).thenReturn(CompletableFuture.completedFuture(member));
        UserNote firstNote = UserNote.builder().userNoteId(new ServerSpecificId(3L, 4L)).user(userInAServer).build();
        UserNote secondNote = UserNote.builder().userNoteId(new ServerSpecificId(3L, 5L)).user(userInAServer).build();
        testUnit.fromNotes(Arrays.asList(firstNote, secondNote));
        verify(self, times(1)).loadFullNotes(any());
    }

    @Test
    public void testLoadingFullNotes() {
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        Member member = Mockito.mock(Member.class);
        UserNote note1 = Mockito.mock(UserNote.class);
        UserNote note2 = Mockito.mock(UserNote.class);
        when(note1.getUser()).thenReturn(userInAServer);
        when(note2.getUser()).thenReturn(userInAServer);
        ServerSpecificId firstUserNoteId = new ServerSpecificId(3L, 4L);
        ServerSpecificId secondUserNoteId = new ServerSpecificId(3L, 5L);
        HashMap<ServerSpecificId, CompletableFuture<Member>> map = new HashMap<>();
        map.put(firstUserNoteId, CompletableFuture.completedFuture(member));
        map.put(secondUserNoteId, CompletableFuture.completedFuture(member));
        when(userNoteManagementService.loadNote(4L, 3L)).thenReturn(note1);
        when(userNoteManagementService.loadNote(5L, 3L)).thenReturn(note2);
        List<NoteEntryModel> models = testUnit.loadFullNotes(map);
        Assert.assertEquals(2, models.size());
        NoteEntryModel firstEntry = models.get(0);
        Assert.assertEquals(member, firstEntry.getFullUser().getMember());
        Assert.assertEquals(userInAServer, firstEntry.getFullUser().getAUserInAServer());
        NoteEntryModel secondEntry = models.get(1);
        Assert.assertEquals(member, secondEntry.getFullUser().getMember());
        Assert.assertEquals(userInAServer, secondEntry.getFullUser().getAUserInAServer());
    }

}
