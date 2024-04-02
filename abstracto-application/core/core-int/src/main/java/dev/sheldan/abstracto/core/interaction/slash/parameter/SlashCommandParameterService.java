package dev.sheldan.abstracto.core.interaction.slash.parameter;


import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.models.database.AEmote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;

public interface SlashCommandParameterService {
    <T, Z> Z getCommandOption(String name, CommandInteractionPayload event, Class<T> parameterType, Class<Z> slashParameterType);
    <T, Z> boolean hasCommandOption(String name, CommandInteractionPayload event, Class<T> parameterType, Class<Z> slashParameterType);
    <T> T getCommandOption(String name, CommandInteractionPayload event, Class<T> parameterType);
    Object getCommandOption(String name, CommandInteractionPayload event);
    Boolean hasCommandOption(String name, CommandInteractionPayload event);
    Boolean hasCommandOptionWithFullType(String name, CommandInteractionPayload event, OptionType optionType);
    AEmote loadAEmoteFromString(String input, Guild guild);
    Emoji loadEmoteFromString(String input, Guild guild);
    List<OptionType> getTypesFromParameter(Parameter parameter);
    List<OptionType> getTypesFromParameter(Class clazz);
    List<OptionType> getTypesFromParameter(Class clazz, boolean strict);
    String getFullQualifiedParameterName(String name, OptionType type);
}
