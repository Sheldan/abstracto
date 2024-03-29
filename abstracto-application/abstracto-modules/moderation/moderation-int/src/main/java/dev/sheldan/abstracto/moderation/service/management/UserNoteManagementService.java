package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.UserNote;

import java.util.List;

public interface UserNoteManagementService {
    UserNote createUserNote(AUserInAServer aUserInAServer, String note);
    void deleteNote(Long id, AServer server);
    void deleteNote(UserNote userNote);
    UserNote loadNote(Long serverId, Long userNoteId);
    boolean noteExists(Long id, AServer server);
    List<UserNote> loadNotesForUser(AUserInAServer aUserInAServer);
    List<UserNote> loadNotesForServer(AServer server);
}
