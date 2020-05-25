package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.moderation.models.database.UserNote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NoteEntryModel {
    private UserNote note;
    private FullUser fullUser;
}