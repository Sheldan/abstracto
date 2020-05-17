package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.models.database.UserNote;
import dev.sheldan.abstracto.moderation.repository.UserNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserNoteManagementServiceBean implements UserNoteManagementService {

    @Autowired
    private UserNoteRepository userNoteRepository;


    @Override
    public UserNote createUserNote(AUserInAServer aUserInAServer, String note) {
        UserNote newNote = UserNote
                .builder()
                .note(note)
                .user(aUserInAServer)
                .build();
        userNoteRepository.save(newNote);
        return newNote;
    }

    @Override
    public void deleteNote(Long id) {
        userNoteRepository.deleteById(id);
    }

    @Override
    public boolean noteExists(Long id) {
        return userNoteRepository.existsById(id);
    }

    @Override
    public List<UserNote> loadNotesForUser(AUserInAServer aUserInAServer) {
        return userNoteRepository.findByUser(aUserInAServer);
    }

    @Override
    public List<UserNote> loadNotesForServer(AServer server) {
        return userNoteRepository.findByUser_ServerReference(server);
    }
}
