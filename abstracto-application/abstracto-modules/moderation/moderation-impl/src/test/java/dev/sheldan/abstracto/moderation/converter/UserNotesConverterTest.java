package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.model.database.UserNote;
import dev.sheldan.abstracto.moderation.model.template.command.NoteEntryModel;
import dev.sheldan.abstracto.moderation.service.management.UserNoteManagementService;
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
    private MemberService memberService;

    @Mock
    private UserNotesConverter self;

    @Mock
    private UserNoteManagementService userNoteManagementService;

    private static final Long SERVER_ID = 3L;
    private static final Long USER_NOTE_ID = 4L;

    @Test
    public void testWithEmptyList() {
        CompletableFuture<List<NoteEntryModel>> entryModels = testUnit.fromNotes(Collections.emptyList());
        Assert.assertEquals(0, entryModels.join().size());
    }

    @Test
    public void testWithSomeUserNotes() {
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        Member member = Mockito.mock(Member.class);
        when(memberService.getMemberInServerAsync(userInAServer)).thenReturn(CompletableFuture.completedFuture(member));
        UserNote firstNote = Mockito.mock(UserNote.class);
        when(firstNote.getUser()).thenReturn(userInAServer);
        UserNote secondNote = Mockito.mock(UserNote.class);
        when(secondNote.getUser()).thenReturn(userInAServer);
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
        ServerSpecificId firstUserNoteId = new ServerSpecificId(SERVER_ID, USER_NOTE_ID);
        ServerSpecificId secondUserNoteId = new ServerSpecificId(SERVER_ID, USER_NOTE_ID + 1);
        HashMap<ServerSpecificId, CompletableFuture<Member>> map = new HashMap<>();
        map.put(firstUserNoteId, CompletableFuture.completedFuture(member));
        map.put(secondUserNoteId, CompletableFuture.completedFuture(member));
        when(userNoteManagementService.loadNote(SERVER_ID, USER_NOTE_ID)).thenReturn(note1);
        when(userNoteManagementService.loadNote(SERVER_ID, USER_NOTE_ID + 1)).thenReturn(note2);
        List<NoteEntryModel> models = testUnit.loadFullNotes(map);
        Assert.assertEquals(2, models.size());
        NoteEntryModel firstEntry = models.get(0);
        Assert.assertEquals(member, firstEntry.getMember());
        NoteEntryModel secondEntry = models.get(1);
        Assert.assertEquals(member, secondEntry.getMember());
    }

}
