package dev.sheldan.abstracto.core.interaction.slash.parameter;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;

public interface SlashCommandAutoCompleteService {
    boolean matchesParameter(AutoCompleteQuery query, String parameterName);
    <T, Z> Z getCommandOption(String name, CommandAutoCompleteInteractionEvent event, Class<T> parameterType, Class<Z> slashParameterType);
    <T, Z> boolean hasCommandOption(String name, CommandAutoCompleteInteractionEvent event, Class<T> parameterType, Class<Z> slashParameterType);
    <T> T getCommandOption(String name, CommandAutoCompleteInteractionEvent event, Class<T> parameterType);
}
