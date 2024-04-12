package dev.sheldan.abstracto.core.interaction.slash.parameter.provider;

import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandOptionTypeMapping;

public interface SlashCommandParameterProvider {
    SlashCommandOptionTypeMapping getOptionMapping();

    default SlashCommandOptionTypeMapping getOptionMapping(Parameter parameter) {
        return getOptionMapping();
    }
}
