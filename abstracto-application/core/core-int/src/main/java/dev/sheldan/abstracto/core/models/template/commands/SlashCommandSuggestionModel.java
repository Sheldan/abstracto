package dev.sheldan.abstracto.core.models.template.commands;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SlashCommandSuggestionModel {
    private String slashCommandPath;
}
