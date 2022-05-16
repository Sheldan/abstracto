package dev.sheldan.abstracto.core.command.slash.parameter.provider.provided;

import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandOptionTypeMapping;
import dev.sheldan.abstracto.core.command.slash.parameter.provider.SlashCommandParameterProvider;
import dev.sheldan.abstracto.core.models.database.AEmote;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class AEmoteSlashCommandParameterProvider implements SlashCommandParameterProvider {
    @Override
    public SlashCommandOptionTypeMapping getOptionMapping() {
        return SlashCommandOptionTypeMapping
                .builder()
                .type(AEmote.class)
                .optionTypes(Arrays.asList(OptionType.STRING))
                .build();
    }
}
