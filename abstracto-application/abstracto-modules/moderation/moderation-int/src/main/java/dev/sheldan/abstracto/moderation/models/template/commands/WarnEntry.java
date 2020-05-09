package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WarnEntry {
    private Warning warning;
    private FullUser warnedUser;
    private FullUser warningUser;
}
