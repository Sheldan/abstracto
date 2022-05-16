package dev.sheldan.abstracto.core.command.slash.parameter.provider;

import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandOptionTypeMapping;

public interface SlashCommandParameterProvider {
    SlashCommandOptionTypeMapping getOptionMapping();
}
