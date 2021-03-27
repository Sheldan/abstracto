package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.CounterService;
import dev.sheldan.abstracto.moderation.model.database.UserNote;
import dev.sheldan.abstracto.moderation.repository.UserNoteRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    private static final Long SERVER_ID = 1L;

    @Mock
    private AServer server;

    @Mock
    private AUserInAServer userInAServer;

    @Test
    public void testCreateUserNote() {
        AUser user = Mockito.mock(AUser.class);
        when(userInAServer.getUserReference()).thenReturn(user);
        when(userInAServer.getServerReference()).thenReturn(server);
        UserNote savedNote = Mockito.mock(UserNote.class);
        ArgumentCaptor<UserNote> noteCaptor = ArgumentCaptor.forClass(UserNote.class);
        when(userNoteRepository.save(noteCaptor.capture())).thenReturn(savedNote);
        UserNote userNote = testUnit.createUserNote(userInAServer, NOTE_TEXT);
        Assert.assertEquals(savedNote, userNote);
        UserNote capturedNote = noteCaptor.getValue();
        Assert.assertEquals(userInAServer, capturedNote.getUser());
        Assert.assertEquals(NOTE_TEXT, capturedNote.getNote());
    }

    @Test
    public void testDeleteNote() {
        when(server.getId()).thenReturn(SERVER_ID);
        testUnit.deleteNote(NOTE_ID, server);
        verify(userNoteRepository, times(1)).deleteByUserNoteId_IdAndUserNoteId_ServerId(NOTE_ID, SERVER_ID);
    }

    @Test
    public void testNoteExists() {
        when(server.getId()).thenReturn(SERVER_ID);
        when(userNoteRepository.existsByUserNoteId_IdAndUserNoteId_ServerId(NOTE_ID, SERVER_ID)).thenReturn(true);
        Assert.assertTrue(testUnit.noteExists(NOTE_ID, server));
    }

    @Test
    public void testLoadNotesForUser() {
        UserNote note = Mockito.mock(UserNote.class);
        UserNote note2 = Mockito.mock(UserNote.class);
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
        UserNote note = Mockito.mock(UserNote.class);
        UserNote note2 = Mockito.mock(UserNote.class);
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
