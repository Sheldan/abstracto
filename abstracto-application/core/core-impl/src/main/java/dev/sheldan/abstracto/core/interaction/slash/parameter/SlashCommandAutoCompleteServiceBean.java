package dev.sheldan.abstracto.core.interaction.slash.parameter;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SlashCommandAutoCompleteServiceBean implements SlashCommandAutoCompleteService {

    @Autowired
    private SlashCommandParameterService slashCommandParameterServiceBean;

    @Override
    public boolean matchesParameter(AutoCompleteQuery query, String parameterName) {
        return query.getName().equalsIgnoreCase(parameterName);
    }

    @Override
    public <T, Z> Z getCommandOption(String name, CommandAutoCompleteInteractionEvent event, Class<T> parameterType, Class<Z> slashParameterType) {
        return slashCommandParameterServiceBean.getCommandOption(name, event, parameterType, slashParameterType);
    }

    @Override
    public <T, Z> boolean hasCommandOption(String name, CommandAutoCompleteInteractionEvent event, Class<T> parameterType, Class<Z> slashParameterType) {
        return slashCommandParameterServiceBean.hasCommandOption(name, event, parameterType, slashParameterType);
    }

    @Override
    public <T> T getCommandOption(String name, CommandAutoCompleteInteractionEvent event, Class<T> parameterType) {
        return getCommandOption(name, event, parameterType, parameterType);
    }
}
