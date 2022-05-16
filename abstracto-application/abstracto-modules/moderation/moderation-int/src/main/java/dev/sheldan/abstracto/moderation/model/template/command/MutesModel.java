package dev.sheldan.abstracto.moderation.model.template.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class MutesModel {
    private List<MuteEntry> mutes;
}
