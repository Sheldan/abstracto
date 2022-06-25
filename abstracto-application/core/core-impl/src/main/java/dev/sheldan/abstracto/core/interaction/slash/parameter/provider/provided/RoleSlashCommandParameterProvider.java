package dev.sheldan.abstracto.core.interaction.slash.parameter.provider.provided;

import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandOptionTypeMapping;
import dev.sheldan.abstracto.core.interaction.slash.parameter.provider.SlashCommandParameterProvider;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class RoleSlashCommandParameterProvider implements SlashCommandParameterProvider {
    @Override
    public SlashCommandOptionTypeMapping getOptionMapping() {
        return SlashCommandOptionTypeMapping
                .builder()
                .type(Role.class)
                .optionTypes(Arrays.asList(OptionType.ROLE))
                .build();
    }
}
