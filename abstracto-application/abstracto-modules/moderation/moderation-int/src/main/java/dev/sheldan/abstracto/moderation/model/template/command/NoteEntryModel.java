package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class NoteEntryModel {
    private String note;
    private Long noteId;
    private Instant created;
    private MemberDisplay member;
    private Long serverId;
}
