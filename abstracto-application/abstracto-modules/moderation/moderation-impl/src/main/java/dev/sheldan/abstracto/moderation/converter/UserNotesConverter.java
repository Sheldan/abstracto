package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.models.database.UserNote;
import dev.sheldan.abstracto.moderation.models.template.commands.NoteEntryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserNotesConverter {
    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private BotService botService;

    public List<NoteEntryModel> fromNotes(List<UserNote> userNotes){
        List<NoteEntryModel> entryModels = new ArrayList<>();
        userNotes.forEach(userNote -> {
            FullUserInServer fullUser = FullUserInServer
                    .builder()
                    .member(botService.getMemberInServer(userNote.getUser()))
                    .aUserInAServer(userNote.getUser())
                    .build();
            NoteEntryModel entryModel = NoteEntryModel
                    .builder()
                    .fullUser(fullUser)
                    .note(userNote)
                    .build();
            entryModels.add(entryModel);
        });
        return entryModels;
    }
}
