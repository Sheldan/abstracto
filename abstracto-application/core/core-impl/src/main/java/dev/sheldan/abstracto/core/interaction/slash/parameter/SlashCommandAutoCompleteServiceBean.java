package dev.sheldan.abstracto.core.interaction.slash.parameter;

import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import org.springframework.stereotype.Component;

@Component
public class SlashCommandAutoCompleteServiceBean implements SlashCommandAutoCompleteService{
    @Override
    public boolean matchesParameter(AutoCompleteQuery query, String parameterName) {
        return query.getName().equalsIgnoreCase(parameterName);
    }
}
