package dev.sheldan.abstracto.core.command.slash.parameter.provider.provided;

import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandOptionTypeMapping;
import dev.sheldan.abstracto.core.command.slash.parameter.provider.SlashCommandParameterProvider;
import dev.sheldan.abstracto.core.models.database.ARole;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ARoleSlashCommandParameterProvider implements SlashCommandParameterProvider {
    @Override
    public SlashCommandOptionTypeMapping getOptionMapping() {
        return SlashCommandOptionTypeMapping
                .builder()
                .type(ARole.class)
                .optionTypes(Arrays.asList(OptionType.ROLE, OptionType.STRING))
                .build();
    }
}
