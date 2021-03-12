package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.CounterService;
import dev.sheldan.abstracto.moderation.model.database.UserNote;
import dev.sheldan.abstracto.moderation.repository.UserNoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UserNoteManagementServiceBean implements UserNoteManagementService {

    @Autowired
    private UserNoteRepository userNoteRepository;

    @Autowired
    private CounterService counterService;

    public static final String USER_NOTE_COUNTER_KEY = "USER_NOTES";

    @Override
    public UserNote createUserNote(AUserInAServer aUserInAServer, String note) {
        Long id = counterService.getNextCounterValue(aUserInAServer.getServerReference(), USER_NOTE_COUNTER_KEY);
        log.info("Creating user note with id {} for user {} in server {}.", id, aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId());
        ServerSpecificId userNoteId = new ServerSpecificId(aUserInAServer.getServerReference().getId(), id);
        UserNote newNote = UserNote
                .builder()
                .note(note)
                .userNoteId(userNoteId)
                .server(aUserInAServer.getServerReference())
                .user(aUserInAServer)
                .build();
        userNoteRepository.save(newNote);
        return newNote;
    }

    @Override
    public void deleteNote(Long id, AServer server) {
        log.info("Deleting user note with id {} in server {}.", id, server.getId());
        userNoteRepository.deleteByUserNoteId_IdAndUserNoteId_ServerId(id, server.getId());
    }

    @Override
    public UserNote loadNote(Long serverId, Long userNoteId) {
        return userNoteRepository.findByUserNoteId_IdAndUserNoteId_ServerId(userNoteId, serverId);
    }

    @Override
    public boolean noteExists(Long id, AServer server) {
        return userNoteRepository.existsByUserNoteId_IdAndUserNoteId_ServerId(id, server.getId());
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
