package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@Setter
public class ListNotesModel extends UserInitiatedServerContext {
    private List<NoteEntryModel> userNotes;
    private FullUserInServer specifiedUser;
}
