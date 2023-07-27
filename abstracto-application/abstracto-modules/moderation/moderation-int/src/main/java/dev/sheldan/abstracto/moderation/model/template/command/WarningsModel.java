package dev.sheldan.abstracto.moderation.model.template.command;

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
public class WarningsModel {
    /**
     * A collection of {@link dev.sheldan.abstracto.moderation.model.database.Warning}s being rendered, might be all warnings of the server, or only the warnings of a specific user
     */
    private List<WarnEntry> warnings;
}
