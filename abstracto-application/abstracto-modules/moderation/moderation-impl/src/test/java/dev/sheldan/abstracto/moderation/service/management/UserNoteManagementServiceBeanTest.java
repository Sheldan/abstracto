package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.CounterService;
import dev.sheldan.abstracto.moderation.models.database.UserNote;
import dev.sheldan.abstracto.moderation.repository.UserNoteRepository;
import dev.sheldan.abstracto.core.test.MockUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserNoteManagementServiceBeanTest {

    @InjectMocks
    private UserNoteManagementServiceBean testUnit;

    @Mock
    private UserNoteRepository userNoteRepository;

    @Mock
    private CounterService counterService;

    private static final String NOTE_TEXT = "noteText";
    private static final Long NOTE_ID = 5L;

    private AServer server;
    private AUserInAServer userInAServer;

    @Before
    public void setup() {
        this.server = MockUtils.getServer();
        this.userInAServer = MockUtils.getUserObject(8L, server);
    }

    @Test
    public void testCreateUserNote() {
        UserNote userNote = testUnit.createUserNote(userInAServer, NOTE_TEXT);
        verify(userNoteRepository, times(1)).save(userNote);
        Assert.assertEquals(userNote.getUser(), userInAServer);
        Assert.assertEquals(userNote.getNote(), NOTE_TEXT);
    }

    @Test
    public void testDeleteNote() {
        testUnit.deleteNote(NOTE_ID, server);
        verify(userNoteRepository, times(1)).deleteByUserNoteId_IdAndUserNoteId_ServerId(NOTE_ID, server.getId());
    }

    @Test
    public void testNoteExists() {
        when(userNoteRepository.existsByUserNoteId_IdAndUserNoteId_ServerId(NOTE_ID, server.getId())).thenReturn(true);
        Assert.assertTrue(testUnit.noteExists(NOTE_ID, server));
    }

    @Test
    public void testLoadNotesForUser() {
        UserNote note = UserNote.builder().build();
        UserNote note2 = UserNote.builder().build();
        List<UserNote> notes = Arrays.asList(note, note2);
        when(userNoteRepository.findByUser(userInAServer)).thenReturn(notes);
        List<UserNote> foundNotes = testUnit.loadNotesForUser(userInAServer);
        Assert.assertEquals(notes.size(), foundNotes.size());
        for (int i = 0; i < foundNotes.size(); i++) {
            UserNote existingNote = notes.get(i);
            UserNote foundNote = foundNotes.get(i);
            Assert.assertEquals(existingNote, foundNote);
        }
    }

    @Test
    public void testLoadNotesForServer() {
        UserNote note = UserNote.builder().build();
        UserNote note2 = UserNote.builder().build();
        List<UserNote> notes = Arrays.asList(note, note2);
        when(userNoteRepository.findByUser_ServerReference(server)).thenReturn(notes);
        List<UserNote> foundNotes = testUnit.loadNotesForServer(server);
        Assert.assertEquals(notes.size(), foundNotes.size());
        for (int i = 0; i < foundNotes.size(); i++) {
            UserNote existingNote = notes.get(i);
            UserNote foundNote = foundNotes.get(i);
            Assert.assertEquals(existingNote, foundNote);
        }
    }
}
