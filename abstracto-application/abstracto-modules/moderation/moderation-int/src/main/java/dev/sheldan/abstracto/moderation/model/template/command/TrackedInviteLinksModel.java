package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import dev.sheldan.abstracto.moderation.model.database.FilteredInviteLink;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class TrackedInviteLinksModel extends SlimUserInitiatedServerContext {
    private List<FilteredInviteLink> inviteLinks;
}
