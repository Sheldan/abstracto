package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Used to render the paginator used to display all the warnings of a user or all users. The template is: "warnings_response_paginator"
 */
@Getter
@Setter
@SuperBuilder
public class WarningsModel extends SlimUserInitiatedServerContext {
    /**
     * A collection of {@link dev.sheldan.abstracto.moderation.models.database.Warning}s being rendered, might be all warnings of the server, or only the warnings of a specific user
     */
    private List<WarnEntry> warnings;
}
