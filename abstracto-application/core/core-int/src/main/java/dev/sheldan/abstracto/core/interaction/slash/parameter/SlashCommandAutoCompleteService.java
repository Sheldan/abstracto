package dev.sheldan.abstracto.core.interaction.slash.parameter;

import net.dv8tion.jda.api.interactions.AutoCompleteQuery;

public interface SlashCommandAutoCompleteService {
    boolean matchesParameter(AutoCompleteQuery query, String parameterName);
}
