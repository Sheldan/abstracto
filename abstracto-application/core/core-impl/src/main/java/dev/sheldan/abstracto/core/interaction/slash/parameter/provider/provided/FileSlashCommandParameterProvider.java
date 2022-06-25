package dev.sheldan.abstracto.core.interaction.slash.parameter.provider.provided;

import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandOptionTypeMapping;
import dev.sheldan.abstracto.core.interaction.slash.parameter.provider.SlashCommandParameterProvider;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;

@Component
public class FileSlashCommandParameterProvider implements SlashCommandParameterProvider {
    @Override
    public SlashCommandOptionTypeMapping getOptionMapping() {
        return SlashCommandOptionTypeMapping
                .builder()
                .type(File.class)
                .optionTypes(Arrays.asList(OptionType.ATTACHMENT))
                .build();
    }
}
