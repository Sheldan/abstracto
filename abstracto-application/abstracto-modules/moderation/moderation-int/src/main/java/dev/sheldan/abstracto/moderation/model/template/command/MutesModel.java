package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class MutesModel extends SlimUserInitiatedServerContext {
    private List<MuteEntry> mutes;
}
