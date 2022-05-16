package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

@Builder
@Getter
@Setter
public class ListNotesModel {
    private List<NoteEntryModel> userNotes;
    private FullUserInServer specifiedUser;
    private Member member;
}
