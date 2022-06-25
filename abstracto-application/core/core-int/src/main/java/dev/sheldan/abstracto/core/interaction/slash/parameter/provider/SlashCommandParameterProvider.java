package dev.sheldan.abstracto.core.interaction.slash.parameter.provider;

import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandOptionTypeMapping;

public interface SlashCommandParameterProvider {
    SlashCommandOptionTypeMapping getOptionMapping();
}
