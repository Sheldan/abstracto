package dev.sheldan.abstracto.core.command.slash.parameter;


import dev.sheldan.abstracto.core.models.database.AEmote;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;

public interface SlashCommandParameterService {
    <T, Z> Z getCommandOption(String name, SlashCommandInteractionEvent event, Class<T> parameterType, Class<Z> slashParameterType);
    <T, Z> boolean hasCommandOption(String name, SlashCommandInteractionEvent event, Class<T> parameterType, Class<Z> slashParameterType);
    <T> T getCommandOption(String name, SlashCommandInteractionEvent event, Class<T> parameterType);
    Object getCommandOption(String name, SlashCommandInteractionEvent event);
    Boolean hasCommandOption(String name, SlashCommandInteractionEvent event);
    Boolean hasCommandOptionWithFullType(String name, SlashCommandInteractionEvent event, OptionType optionType);
    AEmote loadAEmoteFromString(String input, SlashCommandInteractionEvent event);
    Emoji loadEmoteFromString(String input, SlashCommandInteractionEvent event);
    List<OptionType> getTypesFromParameter(Class clazz);
    String getFullQualifiedParameterName(String name, OptionType type);
}
